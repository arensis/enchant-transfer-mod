package net.alfonsormadrid.enchanttransfer.screens.transfertable;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.gui.transfertable.CombineCardSlotPositions;
import net.alfonsormadrid.enchanttransfer.gui.transfertable.SlotPosition;
import net.alfonsormadrid.enchanttransfer.gui.transfertable.TransferItemSlotPositions;
import net.alfonsormadrid.enchanttransfer.services.CombineCardService;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TransferTableScreenHandler extends ScreenHandler {
    private final Inventory combineCardsInput;
    private final Inventory combineCardsOutput;
    private final Inventory transferInput;
    private final Inventory transferItemContent;
    private final ScreenHandlerContext context;
    private final CombineCardService combineCardService;

    public TransferTableScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public TransferTableScreenHandler(int syncId, PlayerInventory playerInventory, final ScreenHandlerContext context) {
        super(EnchantTransferMod.TRANSFER_TABLE_SCREEN_HANDLER, syncId);
        this.combineCardsInput = buildInitInventory(2);
        this.combineCardsOutput = new CraftingResultInventory();
        this.transferInput = buildInitInventory(1);
        this.transferItemContent = buildInitInventory(12);
        this.context = context;

        this.combineCardService = new CombineCardService(
                this.combineCardsInput.getStack(0),
                this.combineCardsInput.getStack(1)
        );

        this.builtCombineCardSlots();
        this.builtTransferSlots();

        addSlotGrid(9, 3, 8, 84, playerInventory, 9);
        addSlotGrid(9, 1, 8, 142, playerInventory, 0);
    }


    public void addSlotGrid(int columnsAmount, int rowsAmount, int startPositionX, int startPositionY, Inventory inventory, int startInventoryIndex) {
        IntStream.range(0, rowsAmount).forEach(rowIndex -> {
            IntStream.range(0, columnsAmount).forEach(columnIndex -> {
                int slotWidth = 18;
                int positionX = startPositionX + columnIndex * slotWidth;
                int positionY = startPositionY + rowIndex * slotWidth;
                int slotIndex = columnIndex + rowIndex * columnsAmount + startInventoryIndex;

                this.addSlot(new Slot(inventory, slotIndex, positionX, positionY));
            });
        });
    }

    private void builtCombineCardSlots() {
        this.addSlot(buildMagicCardSlot(
                0, CombineCardSlotPositions.topCard.positionX, CombineCardSlotPositions.topCard.positionY
        ));
        this.addSlot(buildMagicCardSlot(
                1, CombineCardSlotPositions.bottomCard.positionX, CombineCardSlotPositions.bottomCard.positionY
        ));
        this.addSlot(buildMagiCardResultSlot(
                0, CombineCardSlotPositions.resultCard.positionX, CombineCardSlotPositions.resultCard.positionY
        ));
    }

    private Slot buildMagicCardSlot(int index, int x, int y) {
        return new Slot(this.combineCardsInput, index, x, y) {
            public boolean canInsert(ItemStack stack) {
                return itemIsMagicCard(stack);
            }
            //Logic for onTakeItem method (remove output item)
        };
    }

    private Slot buildMagiCardResultSlot(int index, int x, int y) {
        return new Slot(this.combineCardsOutput, index, x, y) {
            public boolean canInsert(ItemStack stack) {
                return false;
            }
            //Logic for onTakeItem method (remove input items)
        };
    }

    private void builtTransferSlots() {
        this.addSlot(buildTransferInputSlot(
                0, TransferItemSlotPositions.transferItem.positionX, TransferItemSlotPositions.transferItem.positionY
        ));

        this.buildTransferItemContentSlots();
    }

    private void buildTransferItemContentSlots() {
        List<SlotPosition> contentPositions = TransferItemSlotPositions.transferItemContentPositions;
        IntStream.range(0, this.transferItemContent.size())
            .forEach(index ->
                this.addSlot(buildTransferItemContentSlot(
                    index, contentPositions.get(index).positionX, contentPositions.get(index).positionY))
            );
    }

    private Slot buildTransferInputSlot(int index, int x, int y) {
        return new Slot(this.transferInput, index, x, y) {
            public boolean canInsert(ItemStack stack) {
                return stack.isEnchantable();
            }
        };
    }

    private Slot buildTransferItemContentSlot(int index, int x, int y) {
        return new Slot(this.transferItemContent, index, x, y) {
            public boolean canInsert(ItemStack stack) {
                return transferInputIsNotEmpty() && itemIsMagicCard(stack);
            }
            //Logic for onTakeItem (remove enchant of input item)
            //Logic for insert method (add enchant of input item)
        };
    }

    private boolean transferInputIsNotEmpty() {
        return !this.transferInput.getStack(0).isEmpty();
    }

    private SimpleInventory buildInitInventory(int size) {
        return new SimpleInventory(size) {
            public void markDirty(){
                super.markDirty();
                TransferTableScreenHandler.this.onContentChanged(this);
            }
        };
    }

    private boolean itemIsMagicCard(ItemStack stack) {
        return stack.getItem() == EnchantTransferMod.MAGIC_CARD_ITEM;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.combineCardsInput.canPlayerUse(player);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        if (inventory == this.combineCardsInput) {
            updateCombineCardsOutput();
        }

        if (inventory == this.transferInput) {
            this.updatedTransferItemContent();
        }
    }

    private void updatedTransferItemContent() {
        Map<Enchantment, Integer> transferItemEnchantments =
                EnchantmentHelper.get(this.transferInput.getStack(0));

        List<ItemStack> itemMagicCards = transferItemEnchantments.entrySet().stream().map(enchantmentEntry -> {
            ItemStack magicCardEnchanted = new ItemStack(EnchantTransferMod.MAGIC_CARD_ITEM);
            magicCardEnchanted.addEnchantment(enchantmentEntry.getKey(), enchantmentEntry.getValue());
            return magicCardEnchanted;
        }).collect(Collectors.toList());

        IntStream.of(0, itemMagicCards.size()).forEach(index -> {
            this.transferItemContent.setStack(index, itemMagicCards.get(index));
        });
    }

    private void updateCombineCardsOutput() {
        if (this.combineCardService.cardsCanCombine()) {
            this.combineCardsOutput.setStack(0, this.combineCardService.combineCards());
        } else {
            this.combineCardsOutput.setStack(0, ItemStack.EMPTY);
        }

        this.sendContentUpdates();
    }
}
