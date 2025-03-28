package com.example.todolist.form;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.example.todolist.common.Utils;
import com.example.todolist.entity.Task;
import com.example.todolist.entity.Todo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TodoData {

	private Integer id;
	
	@NotBlank(message="件名を入力してください")
	private String title;
	
	@NotNull(message="重要度を選択してください")
	private Integer importance;
	
	@Min(value=0,message="緊急度を選択してください")
	private Integer urgency;
	
	private String deadline;
	
	private String done;
	
	private TaskData newTask;
	
	
	@Valid
	private List<TaskData> taskList;
	
	public Todo toEntity() {
		Todo todo=new Todo();
		todo.setId(id);
		todo.setTitle(title);
		todo.setImportance(importance);
		todo.setUrgency(urgency);
		todo.setDone(done);
		Date deadline=Utils.str2dateOrNull(this.deadline);
		todo.setDeadline(deadline);
		
		Date date;
		Task task;
		
		if(taskList!=null) {
			for(TaskData taskData:taskList) {
				date=Utils.str2dateOrNull(taskData.getDeadline());
				task=new Task(taskData.getId(),
						null,taskData.getTitle(),date,taskData.getDone());
				
				todo.addTask(task);
			}
		}
		
		return todo;
		
	}
	
	
	public TodoData(Todo todo) {
		id=todo.getId();
		title=todo.getTitle();
		importance=todo.getImportance();
		urgency=todo.getUrgency();
		deadline=Utils.date2str(todo.getDeadline());
		done=todo.getDone();
		
		taskList=new ArrayList<>();
		String dt;
		
		for(Task task:todo.getTaskList()) {
			dt=Utils.date2str(task.getDeadline());
			taskList.add(new TaskData(
					task.getId(),task.getTitle(),dt,task.getDone()));
		}
		newTask=new TaskData();
	}
	
	public Task toTaskEntity() {
		Task task=new Task();
		task.setId(newTask.getId());
		task.setTitle(newTask.getTitle());
		task.setDone(newTask.getDone());
		task.setDeadline(Utils.str2date(newTask.getDeadline()));
		
		return task;
	}
	
}
