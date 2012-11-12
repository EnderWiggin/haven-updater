package org.ender.updater;

public interface IUpdaterListener {

    void log(String format);

    void fisnished();

    void progress(long position, long size);

}
