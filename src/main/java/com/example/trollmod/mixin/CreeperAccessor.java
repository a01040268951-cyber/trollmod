package com.example.trollmod.mixin;

import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Creeper.class)
public interface CreeperAccessor {
	@Accessor("swell")
	int trollmod$getSwell();

	@Accessor("swell")
	void trollmod$setSwell(int value);

	@Accessor("oldSwell")
	void trollmod$setOldSwell(int value);
}
