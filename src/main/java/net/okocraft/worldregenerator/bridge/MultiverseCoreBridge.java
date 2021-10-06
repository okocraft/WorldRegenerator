package net.okocraft.worldregenerator.bridge;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;

import org.bukkit.Location;
import org.bukkit.World;

import net.okocraft.worldregenerator.WorldRegeneratorPlugin;

public class MultiverseCoreBridge {
    
    private final WorldRegeneratorPlugin plugin;

    private final MultiverseCore multiverseCore;
    private final MVWorldManager mvWorldManager;
    
    MultiverseCoreBridge(WorldRegeneratorPlugin plugin) {
        this.plugin = plugin;

        this.multiverseCore = MultiverseCore.getPlugin(MultiverseCore.class);
        this.mvWorldManager = multiverseCore.getMVWorldManager();
    }

    public boolean cloneWorld(String oldWorld, String newWorld) {
        return mvWorldManager.cloneWorld(oldWorld, newWorld);
    }

    public boolean regenWorld(String name, long seed, boolean keepGameRules) {
        return mvWorldManager.regenWorld(name, true, false, String.valueOf(seed), keepGameRules);
    }

    public boolean regenWorld(String name, boolean keepGameRules) {
        return mvWorldManager.regenWorld(name, false, false, null, keepGameRules);
    }

    public void setSpawn(String name, Location location) {
        MultiverseWorld world = mvWorldManager.getMVWorld(name);
        if (world != null) {
            world.setSpawnLocation(location);
        }
    }

    public boolean deleteWorld(String name) {
        return mvWorldManager.deleteWorld(name);
    }

    public boolean renameWorld(String oldName, String newName) {
        return !oldName.equalsIgnoreCase(newName) && (cloneWorld(oldName, newName) & deleteWorld(oldName));
    }

    public boolean unloadWorld(String name) {
        return mvWorldManager.unloadWorld(name, true);
    }

    public boolean loadWorld(String name) {
        return mvWorldManager.loadWorld(name);
    }

    public boolean zipWorld(String name) {
        File file;

        World world = plugin.getServer().getWorld(name);
        if (world == null) {
            file = plugin.getServer().getWorldContainer().toPath().resolve(name).toFile();
            if (!file.exists()) {
                return false;
            }
        } else {
            world.save();
            file = world.getWorldFolder();
        }

        File output = file.toPath().getParent()
                .resolve("name_old")
                .resolve(Instant.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME))
                .toFile();
        
        if (!output.getParentFile().mkdirs()) {
            return false;
        }

        try {
            compressFile(output, file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void compressFile(File output, File target) throws IOException {
        if (!target.exists()) {
            throw new IOException("target file does not exists");
        } else if (output.exists()) {
            throw new IOException("output file exists");
        }
    
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output))) {
            compressFile(zos, target);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void compressFile(ZipOutputStream zos, File target) throws IOException {
        if (target.isDirectory()) {
            // 対象がディレクトリの場合
            // 作成するzipファイルにディレクトリのエントリの設定を行う
            zos.putNextEntry(new ZipEntry(target.getPath() + "/"));

            // ディレクトリ内のファイル一覧を取得する
            File[] childFileList = target.listFiles();

            for (File childFile : childFileList) {

                // ディレクトリ内のファイルにて再帰呼び出しする
                compressFile(zos, childFile);
            }

        } else {
            byte[] buf = new byte[1024];
            int len;

            // 対象がファイルの場合
            // 作成するzipファイルにエントリの設定を行う
            zos.putNextEntry(new ZipEntry(target.getPath()));

            // 圧縮するファイル用の入力ストリーム
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(target));

            // 圧縮するファイルを読み込みながら、
            // zipファイル用の出力ストリームへ書き込みをする
            while ((len = bis.read(buf, 0, buf.length)) != -1) {
                zos.write(buf, 0, len);
            }

            // 圧縮するファイル用の入力ストリームを閉じる
            bis.close();

            // エントリを閉じる
            zos.closeEntry();
        }
    }
}
