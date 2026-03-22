package com.agescrafting.agescrafting.compat.campfire;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public record PrimitiveCampfireDisplayRecipe(
        Ingredient input,
        ItemStack cookedOutput,
        ItemStack overcookedOutput,
        int cookTimeTicks,
        int overcookTimeTicks
) {
}
