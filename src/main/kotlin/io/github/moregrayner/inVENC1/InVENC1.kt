package io.github.moregrayner.inVENC1

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import javax.swing.plaf.basic.BasicSliderUI.ScrollListener

@DslMarker
@Target(AnnotationTarget.FUNCTION)
annotation class FlowDSL

class InVENC(private val title: String, private val size: Int) : Listener {

    private val items = mutableMapOf<Int, ItemStack?>()
    private val itemActions = mutableMapOf<Int, (InventoryClickEvent) -> Unit>()
    private val scrollActions = mutableListOf<(ScrollListener) -> Unit>()
    private val movableItems = mutableSetOf<Int>()

    fun setItem(slot: Int, name: String, listLore: List<String>, customModelData: Int, movable: Boolean = true, block: ItemStack.() -> Unit) {
        val item = ItemStack(Material.PAPER)
        val meta = item.itemMeta
        meta?.setDisplayName(name)
        meta?.lore = listLore
        meta?.setCustomModelData(customModelData)
        item.itemMeta = meta
        item.block()
        items[slot] = item

        if (movable) {
            movableItems.add(slot)
        }
    }

    fun onClick(slot: Int, block: (InventoryClickEvent) -> Unit) {
        itemActions[slot] = block
    }

    fun onScroll(scroller: Int, block: () -> Unit) {
        scrollActions.add {
            val stack = 0
            if (stack == scroller) {
                block()
            }
        }
    }

    fun openInventory(player: Player) {
        val inventory: Inventory = Bukkit.createInventory(null, size, title)

        items.forEach { (slot, item) ->
            inventory.setItem(slot, item)
        }

        player.openInventory(inventory)

        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun onInventoryClick(event: InventoryClickEvent) {
                if (event.view.title != title) return

                if (!movableItems.contains(event.slot)) {
                    event.isCancelled = true
                }

                val action = itemActions[event.slot]
                action?.invoke(event)
            }
        }, Bukkit.getPluginManager().plugins.first())
    }
}

@FlowDSL
fun InVENC(title: String, size: Int, block: InVENC.() -> Unit) {
    val inv = InVENC(title, size)
    inv.apply(block)
}
