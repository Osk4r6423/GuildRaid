package ru.guildraid.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GuiHolder implements InventoryHolder {
    public enum Type { MAIN, TREASURY, RAID, STATS }

    private final Type type;
    private Inventory inventory;

    public GuiHolder(Type type) {
        this.type = type;
    }

    public Type getType() { return type; }

    @Override
    public Inventory getInventory() { return inventory; }

    public void setInventory(Inventory inventory) { this.inventory = inventory; }
}