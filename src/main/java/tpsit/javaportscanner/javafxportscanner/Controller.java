package tpsit.javaportscanner.javafxportscanner;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Classe di controllo e iterazione di JavaFX
 */
public class Controller {

    /**
     * La classe utilizzata per lo scanning
     */
    private PortScanner portScanner;

    /**
     * Elenco dei servizi TCP recuperati dalle risorse e resi accessibili all'utente
     */
    private ArrayList<String> serviziTCP = new ArrayList<>();

    /**
     * Elenco dei servizi UDP recuperati dalle risorse e resi accessibili all'utente
     */
    private ArrayList<String> serviziUDP = new ArrayList<>();
    @FXML
    private Button cercaBtn;
    @FXML
    private TextField host;
    @FXML
    private TextField minPorta;
    @FXML
    private TextField maxPorta;
    @FXML
    private TableColumn<Risultato, Integer> cPorte;
    @FXML
    private TableColumn<Risultato, String> cDescrizione;
    @FXML
    private TableView<Risultato> tabellaPorteDescrizione;
    @FXML 
    private Button resetBtn;
    @FXML
    private ChoiceBox<String> protocollo;
    @FXML
    private TextField hostServizi;
    @FXML
    private ChoiceBox<String> protocolloServizi;
    @FXML
    private ChoiceBox<String> servizio;
    @FXML
    private Button resetBtnServizio;
    @FXML
    private Button cercaBtnServizio;
    @FXML
    private ImageView esitoServizio;
    @FXML
    private Text esitoRisultato;
    @FXML
    private Text hostRisultato;
    @FXML
    private Text ipRisultato;
    @FXML
    private Text portaRisultato;
    
    @FXML
    public void initialize() {
        this.portScanner = new PortScanner(this);
        cPorte.setCellValueFactory(new PropertyValueFactory<Risultato, Integer>("porta"));
        cDescrizione.setCellValueFactory(new PropertyValueFactory<Risultato, String>("servizio"));
    }

    /**
     * Metodo per disabilitare l'interfaccia grafica (come input, bottoni, ecc)
     * @param disabilita se disabilitare o meno
     */
    public void disabilitaUI(boolean disabilita){
        Platform.runLater(() -> {
            resetBtn.setDisable(disabilita);
            cercaBtn.setDisable(disabilita);
            host.setDisable(disabilita);
            maxPorta.setDisable(disabilita);
            minPorta.setDisable(disabilita);
            protocollo.setDisable(disabilita);
            
            hostServizi.setDisable(disabilita);
            protocolloServizi.setDisable(disabilita);
            protocollo.setDisable(disabilita);
            resetBtnServizio.setDisable(disabilita);
            cercaBtnServizio.setDisable(disabilita);
        });
    }

    /**
     * Metodo per aggiungere una entry alla tabella dei risultati dei servizi disponibili
     * @param risultato la nuova entry
     */
    public void aggiungiRisultato(Risultato risultato){
        Platform.runLater(() -> {
            this.tabellaPorteDescrizione.getItems().add(risultato);
        });
    }

    /**
     * Metodo per aggiungere i servizi recuperati dalle risorse alla ChoiceBox
     * @param hashMapTCP l'elenco dei servizi TCP associati alle rispettive porte
     * @param hashMapUDP l'elenco dei servizi UDP associati alle rispettive porte
     */
    public void aggiungiOpzioneServizi(HashMap<Integer, String> hashMapTCP,  HashMap<Integer, String> hashMapUDP){
        serviziTCP.addAll(hashMapTCP.values());
        serviziUDP.addAll(hashMapUDP.values());
    }

    /**
     * Metodo per il (ri)caricamento dei servizi in base al protocollo selezionato
     */
    @FXML
    protected void aggiornaServizi() {
        Platform.runLater(() -> {
            if(this.protocolloServizi.getValue().equals("TCP")){
                this.servizio.setValue("");
                this.servizio.getItems().clear();
                this.servizio.getItems().addAll(serviziTCP);
            } else {
                this.servizio.setValue("");
                this.servizio.getItems().clear();
                this.servizio.getItems().addAll(serviziUDP);
            }
        });
    }

    /**
     * Metodo invocato dall'utente per la scansione di un range di porte di un dato host
     */
    @FXML
    protected void ricerca () {
        Platform.runLater(() -> {
            tabellaPorteDescrizione.getItems().clear();
        });
        
        String host = this.host.getText();
        int minPorta;
        int maxPorta;
        
        try {
            minPorta = Integer.parseInt(this.minPorta.getText());
            maxPorta = Integer.parseInt(this.maxPorta.getText());
        } catch (NumberFormatException e){
            minPorta = -1;
            maxPorta = -1;
        }
        
        String protocollo = this.protocollo.getValue();
        
        if(host.isBlank() || protocollo == null || protocollo.isBlank()){
            stampaAvviso("Completare tutti i campi prima di effettuare una scansione!", false);
        } else if(minPorta <= 0 || maxPorta > 65535 || maxPorta < minPorta) {
            stampaAvviso("Range di porte invalido!", true);
        } else {
            disabilitaUI(true);
            if(!this.portScanner.ricerca(host, minPorta, maxPorta, protocollo)){
                stampaAvviso("Protocollo " + protocollo + " non ancora supportato!", true);
            }
        }
    }

    /**
     * Metodo per ripulire i campi input
     */
    @FXML
    protected void resetInput() {
        Platform.runLater(() -> {
            host.setText("");
            maxPorta.setText("");
            minPorta.setText("");
            protocollo.setValue("");
            tabellaPorteDescrizione.getItems().clear(); 
        });
    }

    /**
     * Metodo invocato dall'utente per la verifica di uno specifico servizio
     */
    @FXML
    protected void ricercaServizio() {
        Platform.runLater(() -> {
            esitoServizio.setImage(null);
            esitoRisultato.setText("Avvia la scansione per visualizzare il risultato");
            hostRisultato.setText("");
            ipRisultato.setText("");
            portaRisultato.setText("");
        });
        String hostServizio = this.hostServizi.getText();
        String protocollo = this.protocolloServizi.getValue();
        String servizio = this.servizio.getValue();
        
        if(hostServizio == null || hostServizio.isBlank() || servizio == null || servizio.isBlank() || protocollo == null || protocollo.isBlank()){
            stampaAvviso("Completare tutti i campi prima di effettuare una scansione!", false);
        } else {
            if(!this.portScanner.ricerca(hostServizio, servizio, protocollo)){
                stampaAvviso("Protocollo " + protocollo + " non ancora supportato!", true);
            }
        }
    }

    /**
     * Metodo per ripulire i campi input
     */
    @FXML
    protected void resetInputServizio() {
        Platform.runLater(() -> {
            hostServizi.setText("");
            protocolloServizi.setValue("");
            servizio.setValue("");
            servizio.getItems().clear();
            esitoServizio.setImage(null); 
            esitoRisultato.setText("Avvia la scansione per visualizzare il risultato");
            hostRisultato.setText("");
            ipRisultato.setText("");
            portaRisultato.setText("");
        });
    }

    /**
     * Metodo per la stampa del risultato della scansione
     * @param risultato l'esito della scansione
     * @param host l'{@link java.net.InetAddress} dell'host ricercato
     * @param hostIP l'indirizzo IP dell'host eventualmente risolto
     * @param porta la porta su cui tale servizio è raggiungibile
     */
    public void aggiungiImgRisultato(boolean risultato, String host, String hostIP, int porta){
        if(risultato){
            Platform.runLater(() -> {
                Image img = new Image(Objects.requireNonNull(App.class.getResourceAsStream("successo.png")));
                this.esitoServizio.setImage(img);
                esitoRisultato.setText("ESITO: servizio disponibile");
                hostRisultato.setText("HOST: " + host);
                ipRisultato.setText("INDIRIZZO IP: " + hostIP);
                portaRisultato.setText("PORTA: " + porta);
            });
        } else {
            Platform.runLater(() -> {
                Image img = new Image(Objects.requireNonNull(App.class.getResourceAsStream("insuccesso.png")));
                this.esitoServizio.setImage(img);
                esitoRisultato.setText("ESITO: servizio NON disponibile");
                hostRisultato.setText("HOST: " + host);
                ipRisultato.setText("INDIRIZZO IP: " + hostIP);
                portaRisultato.setText("PORTA: " + porta);
            });
        }
    }

    /**
     * Metodo per la stampa di un generico popup a video per la segnalazione di avvisi
     * @param avviso il testo dell'avviso
     * @param isErrore indicatore per generare avvisi di tipo 'errore'
     */
    public synchronized void stampaAvviso(String avviso, boolean isErrore){
        Platform.runLater(() -> {
            Alert alert = isErrore ? new Alert(Alert.AlertType.ERROR) : new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Errore");
            alert.setHeaderText(null);
            alert.setContentText(avviso);
            alert.showAndWait();
            disabilitaUI(false);
        });
    }
}