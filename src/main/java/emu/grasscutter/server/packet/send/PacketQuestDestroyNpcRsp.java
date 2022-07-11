package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.QuestDestroyNpcRspOuterClass;

public class PacketQuestDestroyNpcRsp extends BasePacket {

    public PacketQuestDestroyNpcRsp(int npcId, int parentQuestId) {
        super(PacketOpcodes.QuestDestroyNpcRsp);

        var proto = QuestDestroyNpcRspOuterClass.QuestDestroyNpcRsp.newBuilder();

        proto.setNpcId(npcId)
            .setParentQuestId(parentQuestId)
            .setRetcode(-1);

        this.setData(proto);
    }
}
