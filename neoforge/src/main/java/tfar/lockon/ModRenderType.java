package tfar.lockon;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class ModRenderType extends RenderStateShard {
    public ModRenderType(String p_i225973_1_, Runnable p_i225973_2_, Runnable p_i225973_3_) {
        super(p_i225973_1_, p_i225973_2_, p_i225973_3_);
    }

    public static final RenderType RENDER_TYPE = getRenderType();
    private static RenderType getRenderType() {
        RenderType.CompositeState renderTypeState = RenderType.CompositeState.builder()
                .setShaderState(POSITION_COLOR_SHADER)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .createCompositeState(false);
        return RenderType.create(LockOn.MODID, DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES, 256, true, true, renderTypeState);
    }
}
