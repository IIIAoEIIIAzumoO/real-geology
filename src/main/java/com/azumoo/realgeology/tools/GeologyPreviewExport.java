package com.azumoo.realgeology.tools;

import com.azumoo.realgeology.RealGeology;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Exports then cleanly stops the dedicated preview server. */
public final class GeologyPreviewExport {
    private GeologyPreviewExport() { }

    public static void onServerStarted(ServerStartedEvent event) {
        if (!Boolean.getBoolean("realgeology.render_sections")) return;
        try {
            for (var output : CrossSectionRenderer.renderAtlas(event.getServer().overworld())) {
                RealGeology.LOGGER.info("Wrote terrain-aware geology cross-section: {}", output);
            }
        } catch (Exception exception) {
            RealGeology.LOGGER.error("Could not write geology cross-section preview", exception);
        }
        event.getServer().execute(() -> event.getServer().halt(false));
    }
}
