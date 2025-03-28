package com.example.todolist.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.example.todolist.common.Utils;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TaskData;
import com.example.todolist.form.TodoData;
import com.example.todolist.form.TodoQuery;
import com.example.todolist.repository.TodoRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TodoService {
	private final TodoRepository todoRepository;
	
	public boolean isValid(TodoData todoData,BindingResult result,
			boolean isCreate) {
		boolean ans=true;
		
		//件名が全角スペースだけならエラー
        if (!Utils.isBlank(todoData.getTitle())) {
            if (Utils.isAllDoubleSpace(todoData.getTitle())) {
                FieldError fieldError = new FieldError(
                    result.getObjectName(),
                    "title",todoData.getTitle(),
                    false,null,null,
                    "件名が全角スペースです");
                result.addError(fieldError);
                ans = false;
            }
        }
		
		
		/*String title=todoData.getTitle();
		if(title!=null&&!title.equals("")) {
			boolean isAllDoubleSpace=true;
			for(int i=0;i<title.length();i++) {
				if(title.charAt(i)!=' ') {
					isAllDoubleSpace=false;
					break;
				}
					
			}
			if(isAllDoubleSpace) {
				FieldError fieldError=new FieldError
						(result.getObjectName(),
								"title","件名が全角スペースです");
						result.addError(fieldError);
						ans=false;
			}
			
		}*/
		
		//期限が過去日ならエラー
		String deadline=todoData.getDeadline();
		if(!deadline.equals("")) {
			//yyyy-mm-dd形式チェック
			if(!Utils.isValidDateFormat(deadline)) {
				FieldError fieldError=new 
						FieldError(result.getObjectName(),
								"deadline",todoData.getDeadline()
								,false,null,null,
								"期限はyyyy-mm-dd形式で入力してください");
						result.addError(fieldError);
				ans=false;
				
			}else {
				//過去日付チェックは新規登録のみ
				if(isCreate) {
					//過去日ならエラー
					if(!Utils.isTodayOrFurtureDate(deadline)) {
						FieldError fieldError=new 
								FieldError(result.getObjectName(),
										"deadline",todoData.getDeadline()
										,false,null,null,
										"期限は今日以降にしてください");
						result.addError(fieldError);
						System.out.println("エラー一覧: " + result.getAllErrors());

						ans=false;
					}
				}
			}
		}
		
		
		/*String deadline=todoData.getDeadline();
		if(!deadline.equals("")) {
			LocalDate today=LocalDate.now();
			LocalDate deadlineDate=null;
			
			try {
				
				deadlineDate=LocalDate.parse(deadline);
				if(isCreate) {
				if(deadlineDate.isBefore(today)) {
					FieldError fieldError=new 
							FieldError(result.getObjectName(),
									"deadline",todoData.getDeadline()
									,false,null,null,
									"期限は今日以降にしてください");
					result.addError(fieldError);
					System.out.println("エラー一覧: " + result.getAllErrors());

					ans=false;
				}}
			}catch(DateTimeException e) {
					FieldError fieldError=new 
							FieldError(result.getObjectName(),
									"deadline",todoData.getDeadline()
									,false,null,null,
									"期限はyyyy-mm-dd形式で入力してください");
							result.addError(fieldError);
					ans=false;
				}
			}*/
		
		// Taskのチェック
        List<TaskData> taskList = todoData.getTaskList();
        if (taskList != null) {
            // すべてのタスクに対して以下を実行する
            // 「タスクのn番目」という情報が必要なので(拡張for文でなく)for文を使用する
            for (int n = 0; n < taskList.size(); n++) {
                TaskData taskData = taskList.get(n);

                // タスクの件名が全角スペースだけで構成されていたらエラー
                if (!Utils.isBlank(taskData.getTitle())) {
                    if (Utils.isAllDoubleSpace(taskData.getTitle())) {
                        FieldError fieldError = new FieldError(
                            result.getObjectName(),
                            "taskList[" + n + "].title",
             
                           "全て全角スペースです");
                        result.addError(fieldError);
                        ans = false;
                    }
                }

                // タスク期限のyyyy-mm-dd形式チェック
                String taskDeadline = taskData.getDeadline();
                if (!taskDeadline.equals("") && !Utils.isValidDateFormat(taskDeadline)) {
                    FieldError fieldError = new FieldError(
                        result.getObjectName(),
                        "taskList[" + n + "].deadline",
                        "yyyy-mm-ddで入力");
                    result.addError(fieldError);
                    ans = false;
                }
            }
        }
		return ans;
	}
	
	public boolean isValid(TodoQuery todoQuery,BindingResult result) {
		boolean ans=true;
		
		String date=todoQuery.getDeadlineFrom();
		if(!date.equals("")) 
			try {
				LocalDate.parse(date);
			}catch(DateTimeException e) {
				FieldError fieldError=new FieldError(
				 result.getObjectName(),"deadlineFrom",todoQuery.getDeadlineFrom(),
				 false,
				    null,
				    null,
				 
						"期限開始はyyyy-mm-dd形式で入力してください");
				result.addError(fieldError);
			ans=false;
			
		}
		date=todoQuery.getDeadlineTo();
		if(!date.equals("")) 
			try {
				LocalDate.parse(date);
			}
			catch(DateTimeException e) {
				FieldError fieldError=new FieldError(
				 result.getObjectName(),"deadlineTo",todoQuery.getDeadlineTo(),
				 false,
				    null,
				    null,
						"期限終了はyyyy-mm-dd形式で入力してください");
				result.addError(fieldError);
				ans=false;
			}
		return ans;
	}
	
	public List<Todo> doQuery(TodoQuery todoQuery){
		List<Todo> todoList=null;
		
		//タイトルで検索
		if(todoQuery.getTitle().length()>0) 
			todoList=todoRepository.findByTitleLike
					("%"+todoQuery.getTitle()+"%");
		
		//重要度で検索
		else if(todoQuery.getImportance()!=null&&todoQuery.getImportance()!=-1)
			todoList=todoRepository.findByImportance
			(todoQuery.getImportance());
		
		//緊急度で検索
		else if(todoQuery.getUrgency()!=null&&todoQuery.getUrgency()!=-1)
			todoList=todoRepository.findByUrgency
			(todoQuery.getUrgency());
		
		else if (!todoQuery.getDeadlineFrom().equals("") && todoQuery.getDeadlineTo().equals("")) {
			// 期限 開始～
			todoList = todoRepository
					.findByDeadlineGreaterThanEqualOrderByDeadlineAsc(Utils.str2date(todoQuery.getDeadlineFrom()));
		} else if (todoQuery.getDeadlineFrom().equals("") && !todoQuery.getDeadlineTo().equals("")) {
			// 期限 ～終了
			todoList = todoRepository
					.findByDeadlineLessThanEqualOrderByDeadlineAsc(Utils.str2date(todoQuery.getDeadlineTo()));
		} else if (!todoQuery.getDeadlineFrom().equals("") && !todoQuery.getDeadlineTo().equals("")) {
			// 期限 開始～終了
			todoList = todoRepository.findByDeadlineBetweenOrderByDeadlineAsc(
					Utils.str2date(todoQuery.getDeadlineFrom()), Utils.str2date(todoQuery.getDeadlineTo()));
		} else if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
			// 完了で検索
			todoList = todoRepository.findByDone("Y");
		} else {
			// 入力条件が無ければ全件検索
			todoList = todoRepository.findAll();
		}
		return todoList;
	
	}
	
	//新規taskチェック
	 public boolean isValid(TaskData taskData, BindingResult result) {
	        boolean ans = true;
	  
	          // タスクの件名が半角スペースだけ or "" ならエラー
	          if (Utils.isBlank(taskData.getTitle())) {
	              FieldError fieldError = new FieldError(
	                  result.getObjectName(),
	                  "newTask.title",
	                  "件名が全角");
	              result.addError(fieldError);
	              ans = false;
	          
	          } else {
	              // タスクの件名が全角スペースだけで構成されていたらエラー
	              if (Utils.isAllDoubleSpace(taskData.getTitle())) {
	                  FieldError fieldError = new FieldError(
	                      result.getObjectName(),
	                      "newTask.title",
	                      "件名が全角スペース");
	                  result.addError(fieldError);
	                  ans = false;
	              }
	          }
	  
	        // 期限が""ならチェックしない
	        String deadline = taskData.getDeadline();
	        if (deadline.equals("")) {
	            return ans;
	        }
	  
	        // 期限のyyyy-mm-dd形式チェック
	        if (!Utils.isValidDateFormat(deadline)) {
	            FieldError fieldError = new FieldError(
	                result.getObjectName(),
	                "newTask.deadline",
	                "yyyy-mm-dd形式で入力");
	            result.addError(fieldError);
	            ans = false;
	  
	        } else {
	            // 過去日付ならエラー
	            if (!Utils.isTodayOrFurtureDate(deadline)) {
	                FieldError fieldError = new FieldError(
	                    result.getObjectName(),
	                    "newTask.deadline",
	                    "過去日");
	                result.addError(fieldError);
	                ans = false;
	            }
	        }
	  
	        return ans;
	    }
	
}
