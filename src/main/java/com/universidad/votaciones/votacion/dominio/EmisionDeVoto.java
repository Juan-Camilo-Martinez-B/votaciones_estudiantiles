package com.universidad.votaciones.votacion.dominio;

public final class EmisionDeVoto {

    private final ReciboDeVoto recibo;
    private final Participacion participacion;
    private final Papeleta papeleta;

    private EmisionDeVoto(ReciboDeVoto recibo, Participacion participacion, Papeleta papeleta) {
        this.recibo = recibo;
        this.participacion = participacion;
        this.papeleta = papeleta;
    }

    public static EmisionDeVoto nueva(Participacion participacion, Papeleta papeleta) {
        return new EmisionDeVoto(ReciboDeVoto.nuevo(participacion), participacion, papeleta);
    }

    public static EmisionDeVoto idempotente(Participacion participacion) {
        return new EmisionDeVoto(ReciboDeVoto.idempotente(participacion), null, null);
    }

    public boolean esNueva() {
        return papeleta != null;
    }

    public ReciboDeVoto recibo() {
        return recibo;
    }

    public Participacion participacion() {
        return participacion;
    }

    public Papeleta papeleta() {
        return papeleta;
    }
}
