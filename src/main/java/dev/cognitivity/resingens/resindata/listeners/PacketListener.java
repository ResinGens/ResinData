package dev.cognitivity.resingens.resindata.listeners;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import dev.cognitivity.resingens.resindata.ResinData;
import org.bukkit.entity.Player;

public class PacketListener extends PacketListenerAbstract {

    public PacketListener() {
        super(PacketListenerPriority.HIGHEST);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        Player player = (Player) event.getPlayer();
        if (player != null && ResinData.getInstance().getData(player) == null && event.getPacketType() != PacketType.Login.Server.DISCONNECT) {
            ResinData.getInstance().createData(player);
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        Player player = (Player) event.getPlayer();
        if (player != null && ResinData.getInstance().getData(player) == null && event.getPacketType() != PacketType.Login.Server.DISCONNECT) {
            ResinData.getInstance().createData(player);
        }
    }
}