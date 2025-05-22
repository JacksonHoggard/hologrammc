package me.jacksonhoggard.holoframes.client;

public interface ItemFrameEntityRenderStateAccessor {
    String holoFrames$getModelFile();

    void holoFrames$setModelFile(String file);

    int holoFrames$getHologramRotation();

    void holoFrames$setHologramRotation(int rotation);
}
