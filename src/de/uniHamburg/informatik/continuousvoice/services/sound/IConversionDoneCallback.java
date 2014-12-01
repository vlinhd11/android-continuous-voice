package de.uniHamburg.informatik.continuousvoice.services.sound;

import java.io.File;

public interface IConversionDoneCallback {
    public void conversionDone(File origin, File converted, long tookMillis);
}