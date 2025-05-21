package cz.novavesodpad.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Manager for handling coroutine tasks
 */
interface TasksManager {
    fun addTask(id: String, task: suspend () -> Unit)
    fun cancelTask(id: String)
    fun cancelAllTasks()
}

/**
 * Implementation of tasks manager
 */
class TasksManagerImpl : TasksManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val tasks = mutableMapOf<String, Job>()
    
    override fun addTask(id: String, task: suspend () -> Unit) {
        // Cancel existing task with the same ID if it exists
        cancelTask(id)
        
        // Create and store a new job
        val job = scope.launch {
            task()
        }
        
        tasks[id] = job
    }
    
    override fun cancelTask(id: String) {
        tasks[id]?.let { job ->
            if (job.isActive) {
                job.cancel()
            }
            tasks.remove(id)
        }
    }
    
    override fun cancelAllTasks() {
        scope.cancel()
        tasks.clear()
    }
}