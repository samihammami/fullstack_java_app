package de.propra.splitter.services;

public class NichtVorhandenException extends RuntimeException{
    public NichtVorhandenException(String message) {
        super(message);
    }
}
