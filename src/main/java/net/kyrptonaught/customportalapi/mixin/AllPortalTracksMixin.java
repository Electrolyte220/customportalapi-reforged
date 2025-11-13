package net.kyrptonaught.customportalapi.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.trains.track.AllPortalTracks;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AllPortalTracks.class)
public class AllPortalTracksMixin {

    @WrapOperation(method = "fromProbe", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    private static Comparable<?> CPAgetAxis(BlockState instance, Property<?> property, Operation<Comparable<?>> original) {
        if(instance.getBlock() instanceof CustomPortalBlock) {
            return instance.getValue(BlockStateProperties.AXIS);
        } else return original.call(instance, property);
    }
}
