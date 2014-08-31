package com.hanzeli.resources;


public class SyncSettings {
    public enum SyncOpt{UPDATE,OVERWRITE,SYNCHRONIZE}
    public enum SyncDrc{LOCREM,REMLOC}

    public SyncOpt option;
    public SyncDrc direction;
}
