package net.okocraft.worldregenerator.scheduler;

import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.bukkit.World;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import net.okocraft.worldregenerator.WorldRegeneratorPlugin;

public class AutoRegenerationScheduler {

    private final String targetWorldName;

    private final Scheduler scheduler;

    public AutoRegenerationScheduler(WorldRegeneratorPlugin plugin, String targetWorldName) throws SchedulerException {
        this.targetWorldName = targetWorldName;
        this.scheduler = StdSchedulerFactory.getDefaultScheduler();

        World targetWorld = plugin.getServer().getWorld(targetWorldName);

        JobDataMap data = new JobDataMap();
        data.put("owning-plugin", plugin);
        data.put("target-world", targetWorldName);
        JobDetail job = JobBuilder.newJob(AutoRegenerationJob.class)
                .withIdentity("regen-" + targetWorldName.toLowerCase(Locale.ROOT) + "-job", "world-regenerator")
                .setJobData(data)
                .build();
        String cronSchedule = plugin.getConfigManager().getMainConfig().getAutoRegenerationSchedule(targetWorld);
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("regen-" + targetWorldName.toLowerCase(Locale.ROOT) + "-trigger", "world-regenerator")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronSchedule).inTimeZone(TimeZone.getDefault()))
                .startNow()
                .build();
        scheduler.scheduleJob(job, Set.of(trigger), true);
        
        scheduler.start();
    }

    public String getTargetWorldName() {
        return targetWorldName;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }
}
