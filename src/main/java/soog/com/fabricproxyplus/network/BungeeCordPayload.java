package soog.com.fabricproxyplus.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record BungeeCordPayload(byte[] data) implements CustomPayload {
    public static final CustomPayload.Id<BungeeCordPayload> ID = new CustomPayload.Id<>(Identifier.of(BungeeCordProtocol.BUNGEECORD_CHANNEL));
    public static final PacketCodec<PacketByteBuf, BungeeCordPayload> CODEC = CustomPayload.codecOf(BungeeCordPayload::write, BungeeCordPayload::new);
    
    private BungeeCordPayload(PacketByteBuf buf) {
        this(buf.readByteArray());
    }
    
    private void write(PacketByteBuf buf) {
        buf.writeByteArray(data);
    }
    
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}