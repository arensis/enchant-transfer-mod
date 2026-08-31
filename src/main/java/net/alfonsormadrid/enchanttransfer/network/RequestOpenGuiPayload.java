package net.alfonsormadrid.enchanttransfer.network;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * C2S payload: the client asks the server to open the GUI for the block
 * at the given position (Transfer Table core or any attached module).
 */
public record RequestOpenGuiPayload(BlockPos blockPos) implements CustomPacketPayload {

    public static final Type<RequestOpenGuiPayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath(EnchantTransferMod.MOD_ID, "request_open_gui"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestOpenGuiPayload> CODEC =
            StreamCodec.of(
                    (buf, p) -> buf.writeBlockPos(p.blockPos()),
                    buf -> new RequestOpenGuiPayload(buf.readBlockPos()));

    @Override
    public Type<RequestOpenGuiPayload> type() {
        return ID;
    }
}
