package emu.grasscutter.scripts.data;

import java.util.List;

import emu.grasscutter.scripts.ScriptLoader;
import lombok.Setter;
import lombok.ToString;
import org.terasology.jnlua.util.AbstractTableMap;

@ToString
@Setter
public class SceneGarbage {
	public List<SceneGadget> gadgets;

	public SceneGarbage(Object garbageData, SceneGroup group){
		var table = (AbstractTableMap)garbageData;
		gadgets = ScriptLoader.getSerializer().toList(SceneGadget.class, table.get("gadgets"));
		gadgets.forEach(m -> m.group = group);
	}
}
