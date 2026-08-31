package net.alfonsormadrid.enchanttransfer.network;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * S2C payload that tells the client to open the Transfer Table selector screen
 * for the given table position.
 */
public record OpenSelectorPayload(BlockPos tablePos) implements CustomPacketPayload {

    public static final Type<OpenSelectorPayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath(EnchantTransferMod.MOD_ID, "open_selector"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenSelectorPayload> CODEC =
            StreamCodec.of(
                    (buf, p) -> buf.writeBlockPos(p.tablePos()),
                    buf -> new OpenSelectorPayload(buf.readBlockPos()));

    @Override
    public Type<OpenSelectorPayload> type() {
        return ID;
    }
}
