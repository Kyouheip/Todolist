package com.example.todolist.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoQuery;

public interface TodoDao {

	//JPQLによる検索メソッド
	Page<Todo>findByJPQL(TodoQuery todoQuery,Pageable pageable);
	
	//Criteria APIによる検索メソッド
	List<Todo> findByCriteria(TodoQuery todoQuery);
}
