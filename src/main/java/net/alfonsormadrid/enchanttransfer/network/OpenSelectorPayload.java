package net.alfonsormadrid.enchanttransfer.network;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * S2C payload that tells the client to open the Transfer Table selector screen
 * for the given table position.
 */
public record OpenSelectorPayload(BlockPos tablePos) implements CustomPayload {

    public static final Id<OpenSelectorPayload> ID =
            new Id<>(Identifier.of(EnchantTransferMod.MOD_ID, "open_selector"));

    public static final PacketCodec<RegistryByteBuf, OpenSelectorPayload> CODEC =
            PacketCodec.ofStatic(
                    (buf, p) -> buf.writeBlockPos(p.tablePos()),
                    buf -> new OpenSelectorPayload(buf.readBlockPos()));

    @Override
    public Id<OpenSelectorPayload> getId() {
        return ID;
    }
}
