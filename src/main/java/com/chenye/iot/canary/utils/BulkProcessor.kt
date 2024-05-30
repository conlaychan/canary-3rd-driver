package com.chenye.iot.canary.utils

import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import javax.annotation.PreDestroy

/**
 * 数据批量延迟处理器
 */
open class BulkProcessor(
    /**
     * 实例名称（线程名称）
     */
    private val name: String? = null,

    /**
     * 任务执行器，不指定时则使用自己的单线程模型
     */
    private val executor: Executor? = null
) {

    private val log = LoggerFactory.getLogger(BulkProcessor::class.java)

    private var thread: Thread? = null
    private val tasks = ConcurrentHashMap<Queue<*>, TaskDefine<*>>()

    @Synchronized
    @PreDestroy
    open fun stop() {
        thread = null
    }

    @Synchronized
    open fun start() {
        if (thread == null) {
            thread = Thread { consume() }
            val t = thread!!
            if (name != null) {
                t.name = name
            }
            t.isDaemon = true
            t.start()
        }
    }

    /**
     * @param queue 待消费的数据队列
     * @param maxChunkSize 每批数据的最大数据量
     * @param maxDelayMills 处理一批数据的最大延迟时间（毫秒）
     * @param processor 如何消费一批数据
     */
    open fun <T> consume(queue: Queue<T>, maxChunkSize: Int, maxDelayMills: Int, processor: (List<T>) -> Unit) {
        tasks[queue] = TaskDefine(queue, maxChunkSize, maxDelayMills, processor)
        start()
    }

    /**
     * 不再消费指定的队列
     */
    open fun cancelConsume(queue: Queue<*>) {
        tasks.remove(queue)
    }

    private data class TaskDefine<T>(
        val queue: Queue<T>,
        val maxChunkSize: Int,
        val maxDelayMills: Int,
        val processor: (List<T>) -> Unit
    ) {
        var chunk = LinkedList<T>()
        var chunkFirstTime: Long = 0
    }

    private fun consume() {
        while (Thread.currentThread() === this.thread) {
            var sleep = true
            for ((_, taskDefine) in tasks) {
                consume(taskDefine)
                if (taskDefine.queue.isNotEmpty()) {
                    sleep = false
                }
            }
            if (sleep) {
                Thread.sleep(1)
            }
        }
    }

    private fun <T> consume(taskDefine: TaskDefine<T>) {
        var execute = false // 数据量达到maxChunkSize 或 首条数据的时间达到maxDelayMills
        val data: T? = taskDefine.queue.poll()

        val chunk = taskDefine.chunk
        if (data != null) {
            chunk.add(data)
            // 先判断数据量
            if (chunk.size == 1) {
                taskDefine.chunkFirstTime = System.currentTimeMillis()
            } else if (chunk.size >= taskDefine.maxChunkSize) {
                execute = true
            }
        }

        // 判断首条数据的时间
        val chunkFirstTime = taskDefine.chunkFirstTime
        if (!execute && chunkFirstTime != 0L && System.currentTimeMillis() - chunkFirstTime >= taskDefine.maxDelayMills) {
            execute = true
        }

        // 处理一批数据
        if (execute && chunk.isNotEmpty()) {
            try {
                if (executor != null) {
                    executor.execute { taskDefine.processor(chunk) }
                } else {
                    taskDefine.processor(chunk)
                }
            } catch (t: Throwable) {
                log.error("执行批量任务失败", t)
            } finally {
                taskDefine.chunk = LinkedList()
                taskDefine.chunkFirstTime = 0
            }
        }
    }
}
