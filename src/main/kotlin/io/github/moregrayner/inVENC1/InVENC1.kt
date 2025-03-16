package io.github.moregrayner.inVENC1


import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*


/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */


//Create Inventory to Instance: with Simple Logic
class InvENC1(plugin: Plugin, title: String, size: Int) : InventoryHolder, Listener {
    private val inventory = Bukkit.createInventory(this, size, title) //Inventory Size
    private val actions: MutableMap<Int, Runnable> = HashMap() //Inventory Click -> Runnable
    private val movableItems: MutableMap<Int, Boolean> = HashMap() //ItemMove:boolean Type(true/false)


    //Default Setting(Inventory)
    init{
        Bukkit.getPluginManager().registerEvents(this, plugin) //Register 'Listener' to instance init
        guiMap[title] = this //Why?: 1 instance, 1 Inventory
        fillEmptySlotsWithBarrier()
    }

    //getInventory Logic
    override fun getInventory(): Inventory {
        return inventory
    }

    // Item to Slot...
    fun setItem(slot: Int, name: String?, lore: List<String>?, customModelData: Int?, amount: Int, movable: Boolean, action: Runnable) {
        if (slot >= 0 && slot < inventory.size && amount > 0) {
            val item = ItemStack(Material.PAPER, amount) // Default Material is PAPER.
            val meta = item.itemMeta //ItemMeta CustomAble

            if (name != null) {
                meta?.setDisplayName(name)
            }

            if (lore != null) {
                meta?.lore = lore
            }

            if (customModelData != null) {
                meta?.setCustomModelData(customModelData)
            }

            item.itemMeta = meta

            inventory.setItem(slot, item)

            actions[slot] = action
            movableItems[slot] = movable
        }
        fillEmptySlotsWithBarrier() //Any Slots to Barrier:AUTO
    }

    // The remaining Slots to Barrier
    private fun fillEmptySlotsWithBarrier() {
        for (i in 0 until inventory.size) {
            if (inventory.getItem(i) == null) {
                val barrier = ItemStack(Material.BARRIER, 1)
                inventory.setItem(i, barrier)
                movableItems[i] = false
            }
        }
    }

    // Open GUI...
    fun open(player: Player) {
        player.openInventory(inventory)
    }

    // Open Storage Inventory to Each Player...
    fun openStorage(player: Player) {
        val uuid = player.uniqueId
        val storage = Bukkit.createInventory(player, 27, "${player.name} 님의 창고")

        // Saved Data Load(HashMap)
        if (storageData.containsKey(uuid)) {
            val items = storageData[uuid]!!
            storage.contents = items
        }

        player.openInventory(storage)
    }

    //InventoryClick Event: must be implements 'Listener'
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.inventory.holder is InvENC1) {
            val slot = event.rawSlot
            val gui = event.inventory.holder as? InvENC1 ?: return

            // Barrier:Default Click Unable!
            if (!gui.movableItems.getOrDefault(slot, true)) {
                event.isCancelled = true
            }

            // Contains Key: to execution
            if (gui.actions.containsKey(slot)) {
                gui.actions[slot]!!.run()
            }
        }
    }


    // Save Items: Do Close Storage Inventory
    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as Player
        val uuid = player.uniqueId
        if (event.view.title == "${player.name} 님의 창고") {
            synchronized(storageData) {
                val items = event.inventory.contents.filterNotNull().toTypedArray()
                storageData[uuid] = items
            }
        }
    }

    // ALL Data...
    companion object {
        private val guiMap: MutableMap<String, InvENC1> = HashMap()
        private val storageData: MutableMap<UUID, Array<ItemStack>> = HashMap()

        fun getGUI(title: String): InvENC1? {
            return guiMap[title]
        }
    }
}
