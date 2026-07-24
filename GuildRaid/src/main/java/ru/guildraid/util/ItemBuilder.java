package ru.guildraid.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    private final ItemStack stack;

    public ItemBuilder(Material mat) {
        this.stack = new ItemStack(mat);
    }

    public ItemBuilder name(String legacy) {
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(legacy));
        stack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(String... lines) {
        ItemMeta meta = stack.getItemMeta();
        List<Component> lore = new ArrayList<>();
        for (String l : lines) {
            lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize(l));
        }
        meta.lore(lore);
        stack.setItemMeta(meta);
        return this;
    }

    public ItemStack build() { return stack; }
}