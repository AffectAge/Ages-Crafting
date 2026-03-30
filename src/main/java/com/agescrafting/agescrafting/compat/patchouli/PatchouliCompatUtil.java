package com.agescrafting.agescrafting.compat.patchouli;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;

public final class PatchouliCompatUtil {
    public static final String PATCHOULI_MODID = "patchouli";
    public static final ResourceLocation GUIDE_BOOK_ITEM_ID = ResourceLocation.fromNamespaceAndPath(PATCHOULI_MODID, "guide_book");
    public static final ResourceLocation BOOK_ID = ResourceLocation.fromNamespaceAndPath("agescrafting", "agescrafting_manual");

    private PatchouliCompatUtil() {
    }

    public static ItemStack createGuideBookStack() {
        if (!ModList.get().isLoaded(PATCHOULI_MODID)) {
            return ItemStack.EMPTY;
        }

        ItemStack apiStack = createBookViaApi();
        if (!apiStack.isEmpty()) {
            return apiStack;
        }

        Item guideBookItem = ForgeRegistries.ITEMS.getValue(GUIDE_BOOK_ITEM_ID);
        if (guideBookItem == null) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(guideBookItem);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("patchouli:book", BOOK_ID.toString());
        return stack;
    }

    private static ItemStack createBookViaApi() {
        try {
            Class<?> apiClass = Class.forName("vazkii.patchouli.api.PatchouliAPI");
            Method getMethod = apiClass.getMethod("get");
            Object api = getMethod.invoke(null);
            Method getBookStack = apiClass.getMethod("getBookStack", ResourceLocation.class);
            Object result = getBookStack.invoke(api, BOOK_ID);
            if (result instanceof ItemStack stack && !stack.isEmpty()) {
                return stack.copy();
            }
        } catch (Throwable ignored) {
            // Fallback to manual tagged guide_book stack.
        }
        return ItemStack.EMPTY;
    }
}
