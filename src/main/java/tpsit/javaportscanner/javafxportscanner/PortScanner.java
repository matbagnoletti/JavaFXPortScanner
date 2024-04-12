package tpsit.javaportscanner.javafxportscanner;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Classe principale di scanning
 */
public class PortScanner {

    /**
     * Elenco dei servizi TCP recuperati dalle risorse in associazione con la propria porta
     */
    private HashMap<Integer, String> serviziTCP = new HashMap<>();
    
    /**
     * Elenco dei servizi UDP recuperati dalle risorse in associazione con la propria porta
     */
    private HashMap<Integer, String> serviziUDP = new HashMap<>();

    /**
     * Controller JavaFX
     */
    private final Controller app;

    /**
     * Threadpool utilizzati per una scansione multipla delle porte
     */
    private ExecutorService threadPool;

    /**
     * Valore temporaneo di porte da analizzare
     */
    private volatile int targetPorte;

    /**
     * Popup alert di avanzamento scansione
     */
    private Alert alert;

    /**
     * Barra di avanzamento scansione
     */
    private ProgressBar progressi;

    /**
     * Testo per l'informativa sull'avanzamento della scansione
     */
    private Label testoAvanzamento;

    /**
     * Indirizzo IP eventualmente risolto dell'host ricercato
     */
    private String ipRisolto;

    /**
     * Elenco temporaneo delle porte analizzate
     */
    private volatile ArrayList<Integer> porteAnalizzate = new ArrayList<Integer>();

    public PortScanner(Controller app) {
        this.app = app;
        popola();
    }

    /**
     * Metodo per il popolamento dei servizi dalle risorse
     */
    private void popola() {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(PortScanner.class.getResourceAsStream("servizi.csv"))))){
            String riga;
            while((riga = bufferedReader.readLine()) != null){
                String[] info = riga.split(";");
                if(info[0].equalsIgnoreCase("TCP")){
                    serviziTCP.put(Integer.parseInt(info[2]), info[1]);
                } else if(info[0].equalsIgnoreCase("UDP")){
                    serviziUDP.put(Integer.parseInt(info[2]), info[1]);
                }
            }

            this.app.aggiungiOpzioneServizi(serviziTCP, serviziUDP);
            
        } catch (IOException e){
            Platform.runLater(() -> {
                app.stampaAvviso("Errore nella lettura del file \"servizi.csv\"", true);
                System.exit(1);
            });
        } catch (ArrayIndexOutOfBoundsException e) {
            Platform.runLater(() -> {
                app.stampaAvviso("Errore nella lettura del file \"servizi.csv\": sintassi di una o più entry errata", true);
                System.exit(1);
            });
        } catch (NullPointerException e){
            Platform.runLater(() -> {
                app.stampaAvviso("Errore nella lettura del file \"servizi.csv\": file non trovato", true);
                System.exit(1);
            });
        }
    }

    /**
     * Metodo per la scansione delle porte
     * @param host l'{@link InetAddress} dell'host da scansionare
     * @param minPorta il numero di porta da cui iniziare la scansione
     * @param maxPorta il numero di porta con cui terminare la scansione
     * @param protocollo il protocollo livello di trasporto da utilizzare (TCP o UDP)
     * @return true se il protocollo inserito è supportato, false altrimenti
     */
    public boolean ricerca(String host, int minPorta, int maxPorta, String protocollo){
        switch (protocollo) {
            case "TCP" -> {
                targetPorte = (maxPorta - minPorta) + 1;
                threadPool = Executors.newFixedThreadPool(50);
                porteAnalizzate.clear();

                if(testHost(host)){

                    popupAvanzamento();

                    for (int i = minPorta; i <= maxPorta; i++){
                        int porta = i;
                        threadPool.execute(() -> {
                            try {
                                Socket socket = new Socket();
                                socket.connect(new InetSocketAddress(host, porta), 100);
                                socket.setSoTimeout(100);
                                socket.close();
                                Risultato risultato;
                                risultato = new Risultato(serviziTCP.getOrDefault(porta, "Servizio non registrato"), porta);
                                app.aggiungiRisultato(risultato);
                                // System.out.println("\033[37m - \033[1;32m " + servizio + " \033[0m\033[37m (porta TCP " + porta + ")\033[0m");
                            } catch (ConnectException | SocketTimeoutException e){
                                // TODO: gestione porte chiuse
                            } catch (IOException e){
                                // TODO: gestione errori porte
                            } finally {
                                fineTest(porta);
                            }
                        });
                    }
                } else {
                    app.stampaAvviso("Host non raggiungibile!", false);
                    app.disabilitaUI(false);
                }
                
                return true;
            }
            
            case "UDP" -> {
                // TODO: udp
                return false;
            }

            default -> {
                return false;
            }
        }
    }

    /**
     * Metodo per segnalare se un dato host è in ascolto offrendo uno specifico servizio
     * @param host l'{@link InetAddress} dell'host da scansionare
     * @param servizio il nome del servizio da ricercare
     * @param protocollo il protocollo livello di trasporto da utilizzare (TCP o UDP)
     * @return true se il protocollo inserito è supportato, false altrimenti
     */
    public boolean ricerca(String host, String servizio, String protocollo){
        switch (protocollo){
            case "TCP" -> {
                if(testHost(host)){
                    popupAvanzamento();

                    int porta = 0;
                    
                    for(Map.Entry<Integer, String> riga : serviziTCP.entrySet()){
                        if(riga.getValue().equals(servizio)){
                            porta = riga.getKey();
                            break;
                        }
                    }
                    
                    if(porta == 0){
                        app.stampaAvviso("Porta del servizio non trovata nelle risorse!", true);
                        app.disabilitaUI(false);
                    } else {
                        targetPorte = 1;
                        porteAnalizzate.clear();
                        
                        try {
                            Socket socket = new Socket();
                            socket.connect(new InetSocketAddress(host, porta), 100);
                            socket.setSoTimeout(1000);
                            socket.close();
                            
                            app.aggiungiImgRisultato(true, host, this.ipRisolto, porta);

                        } catch (Exception e){
                            app.aggiungiImgRisultato(false, host, this.ipRisolto, porta);
                        } finally {
                            fineTest(porta);
                        }
                    }
                } else {
                    app.stampaAvviso("Host non raggiungibile!", false);
                    app.disabilitaUI(false);
                }
                return true;
            }
            
            case "UDP" -> {
                return false;
            }
            
            default -> {
                return false;
            }
        }
    }

    /**
     * Metodo per la creazione del popup di avanzamento della scansione
     */
    private void popupAvanzamento() {
        Platform.runLater(() -> {
            alert = new Alert(Alert.AlertType.NONE);

            testoAvanzamento = new Label("Scansione in corso...");

            // Imposta la barra di avanzamento
            progressi = new ProgressBar();
            progressi.setPrefSize(230, 15);
            progressi.setProgress(0.0);

            // Crea un layout per il dialogo di progresso
            BorderPane pane = new BorderPane();
            pane.setPrefSize(280, 75);
            pane.setCenter(progressi);
            pane.setTop(testoAvanzamento);
            BorderPane.setAlignment(testoAvanzamento, Pos.CENTER);
            BorderPane.setAlignment(progressi, Pos.CENTER);

            alert.getDialogPane().setContent(pane);
            alert.setTitle("Avanzamento");
            alert.setHeaderText(null);
            alert.show();
        });
    }

    /**
     * Metodo invocato da ciascun Thread del {@link #threadPool} in ogni caso al termine della scansione in modo da far aumentare l'avanzamento
     * @param porta il numero di porta scansionato
     */
    public synchronized void fineTest(int porta){
        porteAnalizzate.add(porta);
        double progressi = (double) porteAnalizzate.size() / (double) targetPorte;

        Platform.runLater(() -> {
            this.progressi.setProgress(progressi);
            this.testoAvanzamento.setText("Scansione in corso... " + (int)(progressi * 100) + " %");
        });

        if(porteAnalizzate.size() == targetPorte){
            Platform.runLater(() -> {
                this.testoAvanzamento.setText("Scansione porte terminata!");
            });
            
            if(threadPool != null) {
                threadPool.shutdown();
                try {
                    if (!threadPool.awaitTermination(1500, TimeUnit.MILLISECONDS)) {
                        threadPool.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    threadPool.shutdownNow();
                    Thread.currentThread().interrupt();
                } finally {
                    app.disabilitaUI(false);
                    Platform.runLater(() -> {
                        alert.getDialogPane().getScene().getWindow().hide();
                    });
                }
            } else {
                app.disabilitaUI(false);
                Platform.runLater(() -> {
                    alert.getDialogPane().getScene().getWindow().hide();
                });
            }
        }
    }

    /**
     * Metodo per verificare l'esistenza di un {@link InetAddress}
     * @param host l'{@link InetAddress} dell'host da risolvere
     * @return true se l'host è stato risolto, false altrimenti
     */
    private boolean testHost(String host){
        try {
            InetAddress test = InetAddress.getByName(host);
            this.ipRisolto = test.getHostAddress();
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
