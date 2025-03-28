package com.example.todolist.entity;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.ToString;

@Entity
@Table(name="todo")
@Data
@ToString(exclude="taskList")
public class Todo {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	private String title;
	private int importance;
	private int urgency;
	private Date deadline;
	private String done;
	
	@OneToMany(mappedBy="todo",cascade=CascadeType.ALL)
	@OrderBy("id asc")
	private List<Task> taskList=new ArrayList<>();
	
	public void addTask(Task task) {
		task.setTodo(this);
		taskList.add(task);
	}
}
