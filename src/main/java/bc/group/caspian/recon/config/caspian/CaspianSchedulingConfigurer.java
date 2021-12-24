package bc.group.caspian.recon.config.caspian;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;


@Configuration
public class CaspianSchedulingConfigurer implements SchedulingConfigurer {

    @Value("${caspian.taskPoolThreadSize:10}")
    private int POOL_SIZE;

    /* (non-Javadoc)
     * @see org.springframework.scheduling.annotation.SchedulingConfigurer#configureTasks(org.springframework.scheduling.config.ScheduledTaskRegistrar)
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();

        threadPoolTaskScheduler.setPoolSize(POOL_SIZE);
        threadPoolTaskScheduler.setThreadNamePrefix("caspian-taskthread-");
        threadPoolTaskScheduler.initialize();

        scheduledTaskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
    }

}