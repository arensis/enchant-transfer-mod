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
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BookItem;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import java.util.Map.Entry;

import java.util.*;
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
        this.combineCardsOutput = buildInitInventory(1);
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
            Map<Enchantment, Integer> enchants = EnchantmentHelper.get(this.transferInput.getStack(0));
            updateTransferItemContent(enchants);
        }
    }

    /**
     * -- Nuevos métodos --
     */

    private void updateTransferItemContent(Map<Enchantment, Integer> enchants) {
        addMagiCardsToTransferItemContent(buildMagicCardsFromEnchants(enchants));
    }

    private void addNewEnchantToItem() {
        Map<Enchantment, Integer> enchants = mapItemStacksToEnchants(getAllTransferItemContentStacks());
        updateTransferInputStack(enchants);
    }

    private void removeEnchantFromTransferInput(ItemStack stack) {
        updateTransferInputStack(itemEnchantsFilteredBy(stack));
    }

    private void updateTransferInputStack(Map<Enchantment, Integer> enchants) {
        this.transferInput.setStack(0, createCopyTransferInputWith(enchants));
    }

    private ItemStack createCopyTransferInputWith(Map<Enchantment, Integer> enchants) {
        ItemStack copyTransferInput = new ItemStack(this.transferInput.getStack(0).getItem());
        enchants.forEach(copyTransferInput::addEnchantment);
        return copyTransferInput;
    }

    private Map<Enchantment, Integer> itemEnchantsFilteredBy(ItemStack stack) {
        return EnchantmentHelper.get(this.transferInput.getStack(0))
                .entrySet()
                .stream()
                .filter(entry -> !containsEnchantment(stack, entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private void removeAllTransferContentItemStacks() {
        IntStream.range(0, this.transferItemContent.size()).forEach(this.transferItemContent::removeStack);
    }

    private void addMagiCardsToTransferItemContent(List<ItemStack> magicCards) {
        IntStream.range(0, magicCards.size())
                .forEach(index -> this.transferItemContent.setStack(index, magicCards.get(index)));
    }

    private Map<Enchantment, Integer> mapItemStacksToEnchants(List<ItemStack> stacks) {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        stacks.forEach(item -> EnchantmentHelper.get(item).forEach(enchants::put));
        return enchants;
    }

    private List<ItemStack> getAllTransferItemContentStacks() {
        return IntStream.range(0, this.transferItemContent.size())
                .mapToObj(this.transferItemContent::getStack)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * FIN DE MÉTODOS NUEVOS
     */

    private List<ItemStack> buildMagicCardsFromEnchants(Map<Enchantment, Integer> enchants) {
        return enchants.entrySet().stream().map(enchantmentEntry -> {
            ItemStack magicCardEnchanted = new ItemStack(EnchantTransferMod.MAGIC_CARD_ITEM);
            magicCardEnchanted.addEnchantment(enchantmentEntry.getKey(), enchantmentEntry.getValue());
            return magicCardEnchanted;
        }).collect(Collectors.toList());
    }

    private void updateCombineCardsOutput() {
        combineCardService.setCard1(this.combineCardsInput.getStack(0));
        combineCardService.setCard2(this.combineCardsInput.getStack(1));

        if (this.combineCardService.cardsCanCombine()) {
            this.combineCardsOutput.setStack(0, this.combineCardService.combineCards());
        } else {
            this.combineCardsOutput.setStack(0, ItemStack.EMPTY);
        }

        this.sendContentUpdates();
    }

    private boolean containsEnchantment(ItemStack stack, Enchantment enchantment) {
        return EnchantmentHelper.get(stack).containsKey(enchantment);
    }

    private SimpleInventory buildInitInventory(int size) {
        return new SimpleInventory(size) {
            public void markDirty(){
                super.markDirty();
                TransferTableScreenHandler.this.onContentChanged(this);
            }
        };
    }

    private void builtCombineCardSlots() {
        this.addSlot(buildMagicCardSlot(
                0, CombineCardSlotPositions.topCard.positionX, CombineCardSlotPositions.topCard.positionY
        ));
        this.addSlot(buildMagicCardSlot(
                1, CombineCardSlotPositions.bottomCard.positionX, CombineCardSlotPositions.bottomCard.positionY
        ));
        this.addSlot(buildMagiCardResultSlot(
                CombineCardSlotPositions.resultCard.positionX, CombineCardSlotPositions.resultCard.positionY
        ));
    }

    private Slot buildMagicCardSlot(int index, int x, int y) {
        return new Slot(this.combineCardsInput, index, x, y) {
            public boolean canInsert(ItemStack stack) {
                return itemIsMagicCard(stack);
            }
        };
    }

    private Slot buildMagiCardResultSlot(int x, int y) {
        return new Slot(this.combineCardsOutput, 0, x, y) {
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
                removeCombineCardsInputStack();
                return super.onTakeItem(player, stack);
            }
        };
    }

    private void removeCombineCardsInputStack() {
        IntStream.range(0, this.combineCardsInput.size()).forEach(this.combineCardsInput::removeStack);
    }

    private void builtTransferSlots() {
        this.addSlot(buildTransferInputSlot(
                TransferItemSlotPositions.transferItem.positionX, TransferItemSlotPositions.transferItem.positionY
        ));

        this.buildTransferItemContentSlots();
    }

    private Slot buildTransferInputSlot( int x, int y) {
        return new Slot(this.transferInput, 0, x, y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isEnchantable() || stack.hasEnchantments() || stack.getItem() instanceof EnchantedBookItem;
            }

            @Override
            public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
                removeAllTransferContentItemStacks();
                return super.onTakeItem(player, stack);
            }
        };
    }

    private boolean containsTheSameEnchant(ItemStack stack) {
        return mapItemStacksToEnchants(getAllTransferItemContentStacks())
                .entrySet()
                .stream()
                .anyMatch(entry-> containsEnchantment(stack, entry.getKey()));
    }

    private void buildTransferItemContentSlots() {
        List<SlotPosition> contentPositions = TransferItemSlotPositions.transferItemContentPositions;
        IntStream.range(0, this.transferItemContent.size())
                .forEach(index ->
                        this.addSlot(buildTransferItemContentSlot(
                                index, contentPositions.get(index).positionX, contentPositions.get(index).positionY))
                );
    }

    private Slot buildTransferItemContentSlot(int index, int x, int y) {
        return new Slot(this.transferItemContent, index, x, y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return transferInputIsNotEmpty() && itemIsMagicCard(stack) && !containsTheSameEnchant(stack);
            }

            @Override
            public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
                removeEnchantFromTransferInput(stack);
                return super.onTakeItem(player, stack);
            }

            @Override
            public void setStack(ItemStack itemStack) {
                super.setStack(itemStack);
                addNewEnchantToItem();
            }
        };
    }

    public void addSlotGrid(int columnsAmount, int rowsAmount, int startPositionX, int startPositionY, Inventory inventory, int startInventoryIndex) {
        IntStream.range(0, rowsAmount)
                .forEach(rowIndex -> IntStream.range(0, columnsAmount)
                        .forEach(columnIndex -> {
                            int slotWidth = 18;
                            int positionX = startPositionX + columnIndex * slotWidth;
                            int positionY = startPositionY + rowIndex * slotWidth;
                            int slotIndex = columnIndex + rowIndex * columnsAmount + startInventoryIndex;

                            this.addSlot(new Slot(inventory, slotIndex, positionX, positionY));
        }));
    }

    private boolean transferInputIsNotEmpty() {
        return !this.transferInput.getStack(0).isEmpty();
    }

    private boolean itemIsMagicCard(ItemStack stack) {
        return stack.getItem() == EnchantTransferMod.MAGIC_CARD_ITEM;
    }
}
