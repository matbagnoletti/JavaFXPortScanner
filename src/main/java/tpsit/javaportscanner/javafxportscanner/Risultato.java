package tpsit.javaportscanner.javafxportscanner;

public class Risultato {
    public final String servizio;
    public final int porta;
    public Risultato(String servizio, int porta){
        this.servizio = servizio;
        this.porta = porta;
    }

    public String getServizio() {
        return servizio;
    }

    public int getPorta() {
        return porta;
    }
}
