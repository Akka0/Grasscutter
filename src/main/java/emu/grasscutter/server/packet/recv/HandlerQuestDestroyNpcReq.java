package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.QuestDestroyNpcReqOuterClass;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketQuestDestroyNpcRsp;

@Opcodes(PacketOpcodes.QuestDestroyNpcReq)
public class HandlerQuestDestroyNpcReq extends PacketHandler {

	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        var req = QuestDestroyNpcReqOuterClass.QuestDestroyNpcReq.parseFrom(payload);

		session.send(new PacketQuestDestroyNpcRsp(req.getNpcId(), req.getParentQuestId()));
	}

}
