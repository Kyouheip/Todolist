package com.example.todolist.controller;


import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.todolist.dao.TodoDaoImpl;
import com.example.todolist.entity.Task;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoData;
import com.example.todolist.form.TodoQuery;
import com.example.todolist.repository.TaskRepository;
import com.example.todolist.repository.TodoRepository;
import com.example.todolist.service.TodoService;

import lombok.AllArgsConstructor;


@Controller
@AllArgsConstructor
public class TodolistController {

	private final TodoRepository todoRepository;
	private final TodoService todoService;
	private final HttpSession session;
	private final TodoDaoImpl todoDaoImpl;
	private final TaskRepository taskRepository;
	
	//ToDo一覧表示
	@GetMapping("/todo")
	public ModelAndView showTodoList(ModelAndView mv,
			@PageableDefault(page=0,size=5,sort="id") Pageable pageable) {
		mv.setViewName("todoList");
		//セッションから前回検索結果を取得
		TodoQuery todoQuery=(TodoQuery)session.getAttribute("todoQuery") ;
		//無ければ初期値を使う
		if(todoQuery==null) {
			todoQuery=new TodoQuery();
			session.setAttribute("todoQuery",new TodoQuery());
		}
		//セッションから前回pageableを取得
		Pageable prevPageable=(Pageable)session.getAttribute("prevPageable");
		//無ければ@PageableDefaultを使う
		if(prevPageable==null) {
			prevPageable=pageable;
			session.setAttribute("prevPageable", pageable);
		}
		Page<Todo> todoPage=todoDaoImpl.findByJPQL(todoQuery, prevPageable);
		mv.addObject("todoPage",todoPage);
		mv.addObject("todoList",todoPage.getContent());
		mv.addObject("todoQuery",todoQuery);
		return mv;
	}
	
	//新規追加
	@PostMapping("/todo/create/form")
	public ModelAndView createTodo(ModelAndView mv) {
		mv.setViewName("todoForm");
		mv.addObject("todoData",new TodoData());
		session.setAttribute("mode","create");
		return mv;
		
	}
	
	//登録
	@PostMapping("/todo/create/do")
	public String createTodo
	(@ModelAttribute @Validated TodoData todoData,
			BindingResult result) {
		
		//@Validatedで拾えないエラーを確認
		boolean isValid=todoService.isValid(todoData,result,true);
		
		if(!result.hasErrors()&&isValid) {
			Todo todo=todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			return "redirect:/todo/"+todo.getId();
		}
		else {
			/*mv.addObject("todoData",todoData)は
			@ModelAttributeでモデルに格納されているため省略できる*/
			return "todoForm";
		}
	}
	
	//更新ボタンが押されたとき
	@PostMapping("/todo/update")
	public String updateTodo(@ModelAttribute @Validated TodoData todoData,
			BindingResult result) {
		
		//@Validatedで拾えないエラーを確認
		boolean isValid=todoService.isValid(todoData,result,false);
		
		if(!result.hasErrors()&&isValid) {
			Todo todo=todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			return "redirect:/todo/"+todo.getId();
			}
		else {
			/*mv.addObject("todoData",todoData)は
			@ModelAttributeでモデルに格納されているため省略できる*/
			return "todoForm";
		}
	}
	
	//キャンセルボタンが押されたとき
	@PostMapping("/todo/cancel")
	public String cancel() {
		return "redirect:/todo";
	}
	
	//件名がクリックされたとき
	@GetMapping("/todo/{id}")
	public ModelAndView todoById(@PathVariable int id,ModelAndView mv) {
		mv.setViewName("todoForm");
		Todo todo=todoRepository.findById(id).get();
		mv.addObject("todoData",new TodoData(todo));
		//mv.addObject("todoData",todo);
		session.setAttribute("mode","update");
		return mv;
	}
	
	//削除ボタンが押されたとき
	@PostMapping("/todo/delete")
	public String deleteTodo(@ModelAttribute TodoData todoData) {
		todoRepository.deleteById(todoData.getId());
		return "redirect:/todo";
	}
	
	//検索
	@PostMapping("/todo/query")
	public ModelAndView queryTodo(@ModelAttribute
			TodoQuery todoQuery,BindingResult result,
			@PageableDefault(page=0,size=5) Pageable pageable,ModelAndView mv) {
		mv.setViewName("todoList");
		
		Page<Todo> todoPage=null;
		if(todoService.isValid(todoQuery,result)) {
			//todolist=todoService.doQuery(todoQuery);
			todoPage=todoDaoImpl.findByJPQL(todoQuery,pageable);
		
		mv.addObject("todoPage",todoPage)	;
		mv.addObject("todoList",todoPage.getContent());
		session.setAttribute("todoQuery",todoQuery);
		}else{
			mv.addObject("todoPage",null);
			mv.addObject("todoList",null);
			
		}
		return mv;
		
	}
	//ページ番号が押されたとき
	@GetMapping("/todo/query")
	public ModelAndView queryTodo(@PageableDefault(page=0,size=5,sort="id")
			Pageable pageable,ModelAndView mv) {
		
		session.setAttribute("prevPageable",pageable);
		mv.setViewName("todoList");
		
		//セッションに保存されている条件で検索
		TodoQuery todoQuery=(TodoQuery)session.getAttribute("todoQuery");
		Page<Todo> todoPage=todoDaoImpl.findByJPQL(todoQuery, pageable);
		
		mv.addObject("todoQuery",todoQuery);
		mv.addObject("todoPage",todoPage);
		mv.addObject("todoList",todoPage.getContent());
		
		return mv;
		
	}
	
	
	//task削除ボタン
	@GetMapping("/task/delete")
	public String deleteTask(int taskId,int todoId) {
		
		taskRepository.deleteById(taskId);
		return "redirect:/todo/"+todoId;
		
	}
	
	//task登録ボタン
	@PostMapping("/task/create")
	public String createTask(@ModelAttribute TodoData todoData,
							BindingResult result) {
		boolean isValid=todoService.isValid(todoData.getNewTask(), result);
		if(isValid) {
			Todo todo=todoData.toEntity();
			Task task=todoData.toTaskEntity();
			task.setTodo(todo);
			taskRepository.saveAndFlush(task);
			 return "redirect:/todo/"+todo.getId();
		}else {
			return "todoForm";
		}
	}
}
