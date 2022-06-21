package emu.grasscutter.scripts;

import com.esotericsoftware.reflectasm.MethodAccess;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.props.EntityType;
import emu.grasscutter.scripts.constants.EventType;
import emu.grasscutter.scripts.constants.ScriptGadgetState;
import emu.grasscutter.scripts.constants.ScriptRegionShape;
import emu.grasscutter.scripts.data.SceneMeta;
import emu.grasscutter.scripts.serializer.LuaSerializer;
import emu.grasscutter.scripts.serializer.Serializer;
import net.sandius.rembulan.compiler.CompilerChunkLoader;
import org.terasology.jnlua.JavaFunction;
import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.NamedJavaFunction;
import org.terasology.jnlua.script.CompiledLuaScript;
import org.terasology.jnlua.script.LuaBindings;
import org.terasology.jnlua.script.LuaScriptEngine;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngineManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ScriptLoader {
	private static ScriptEngineManager sm;
	private static LuaScriptEngine engine;
	private static String fileType;
	private static Serializer serializer;
	private static ScriptLib scriptLib;

	private static ScriptEngineManager manager = new ScriptEngineManager();
	/**
	 * suggest GC to remove it if the memory is less
	 */
	private static Map<String, SoftReference<CompiledScript>> scriptsCache = new ConcurrentHashMap<>();
	/**
	 * sceneId - SceneMeta
	 */
	private static Map<Integer, SoftReference<SceneMeta>> sceneMetaCache = new ConcurrentHashMap<>();


	public synchronized static void init() throws Exception {
		if (sm != null) {
			throw new Exception("Script loader already initialized");
		}

		ScriptEngineManager manager = new ScriptEngineManager();

		// Create script engine
		sm = new ScriptEngineManager();
        engine =(LuaScriptEngine) manager.getEngineByName("Lua");
        
        // Lua stuff
        fileType = "lua";
        serializer = new LuaSerializer();
        
        // Set engine to replace require as a temporary fix to missing scripts
        ScriptContext ctx = (ScriptContext) engine.getContext();

		engine.put("require", new JavaFunction() {
			@Override
			public int invoke(LuaState luaState) {
				return 0;
			}
		});

		//engine.put("EntityType",
		//		Arrays.stream(EntityType.values()).collect(Collectors.toMap(e -> e.name().toUpperCase(), EntityType::getValue)));

		//engine.put("EventType", new EventType());
		//engine.put("GadgetState", new ScriptGadgetState());
		//engine.put("RegionShape", new ScriptRegionShape());

		ScriptBinding.coerce(engine, "ScriptLib", new ScriptLib());
		ScriptBinding.coerce(engine, "EventType", new EventType());
		ScriptBinding.coerce(engine, "RegionShape", new ScriptRegionShape());
		ScriptBinding.coerce(engine, "GadgetState", new ScriptGadgetState());
		ScriptBinding.coerce(engine, "EntityType", Arrays.stream(EntityType.values()).collect(Collectors.toMap(e -> e.name().toUpperCase(), EntityType::getValue)));
	}
	
	public static LuaScriptEngine getEngine() {
		return engine;
	}
	
	public static String getScriptType() {
		return fileType;
	}

	public static Serializer getSerializer() {
		return serializer;
	}


	public static <T> Optional<T> tryGet(SoftReference<T> softReference){
		try{
			return Optional.ofNullable(softReference.get());
		}catch (NullPointerException npe){
			return Optional.empty();
		}
	}
	public static CompiledScript getScriptByPath(String path) {
		var sc = tryGet(scriptsCache.get(path));
		if (sc.isPresent()) {
			return sc.get();
		}

		Grasscutter.getLogger().info("Loading script " + path);
		File file = new File(path);
		if (!file.exists()) return null;

		try {
			var binding = new LuaBindings(getEngine());
			var L = binding.getLuaState();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			synchronized(L) {
				L.load((String)Files.readString(file.toPath()), file.getName());
				try {
					L.dump(out, false);
				} finally {
					L.pop(1);
				}
			}
			var script = new CompiledLuaScript(getEngine(), out.toByteArray());
			scriptsCache.put(path, new SoftReference<>(script));
			return script;
		} catch (Exception e) {
			Grasscutter.getLogger().error("Loading script {} failed!", path, e);
			return null;
		}
	}

	public static SceneMeta getSceneMeta(int sceneId) {
		return tryGet(sceneMetaCache.get(sceneId)).orElseGet(() -> {
			var instance = SceneMeta.of(sceneId);
			sceneMetaCache.put(sceneId, new SoftReference<>(instance));
			return instance;
		});
	}

}
