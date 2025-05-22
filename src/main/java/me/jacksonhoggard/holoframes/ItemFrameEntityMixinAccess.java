package me.jacksonhoggard.holoframes;

public interface ItemFrameEntityMixinAccess {

    String holoFrames$getModelFile();

    void holoFrames$setModelFile(String fileName);

    int holoFrames$getHologramRotation();

    void holoFrames$setHologramRotation(int rotation);

}
