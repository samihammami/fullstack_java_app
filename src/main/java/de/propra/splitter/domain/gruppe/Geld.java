package de.propra.splitter.domain.gruppe;

class Geld {

    private Double betrag;

    public Geld() {
    }

    public Geld(Double betrag) {
        this.betrag = betrag;
    }

    public Double getBetrag(){
        return betrag;
    }

    public void setBetrag(Double betrag){
        runden(betrag);
    }

    public void addieren(Double summand) {
        betrag += summand;
        runden(betrag);
    }

    public void subtrahieren(Double subtrahend) {
        betrag -= subtrahend;
        runden(betrag);
    }

    public void multiplizieren(Double faktor) {
        betrag *= faktor;
        runden(betrag);
    }

    public void dividieren(Double divisor) {
        betrag /= divisor;
        runden(betrag);
    }

    public int toCent(){
        return (int)(betrag * 100);
    }
	
	private void runden(Double betrag){
        this.betrag = ((int) (betrag * 100)) / 100.0;
    }
}