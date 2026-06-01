package net.alfonsormadrid.enchanttransfer.network;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * C2S payload: the client asks the server to open the GUI for the block
 * at the given position (Transfer Table core or any attached module).
 */
public record RequestOpenGuiPayload(BlockPos blockPos) implements CustomPayload {

    public static final Id<RequestOpenGuiPayload> ID =
            new Id<>(Identifier.of(EnchantTransferMod.MOD_ID, "request_open_gui"));

    public static final PacketCodec<RegistryByteBuf, RequestOpenGuiPayload> CODEC =
            PacketCodec.ofStatic(
                    (buf, p) -> buf.writeBlockPos(p.blockPos()),
                    buf -> new RequestOpenGuiPayload(buf.readBlockPos()));

    @Override
    public Id<RequestOpenGuiPayload> getId() {
        return ID;
    }
}
