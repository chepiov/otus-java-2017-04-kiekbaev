package ru.otus.chepiov.l11;

/**
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public interface SoftRefCacheEngineMBean {

    public int getHitCount();

    public int getMissCount();
}
