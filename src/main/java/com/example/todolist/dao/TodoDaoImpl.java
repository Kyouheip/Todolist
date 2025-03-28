package com.example.todolist.dao;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.todolist.common.Utils;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoQuery;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class TodoDaoImpl implements TodoDao {

	private final EntityManager entityManager;
	
	//JPQLによる検索
	@Override
	public Page<Todo>findByJPQL(TodoQuery todoQuery,Pageable pageable){
		StringBuilder sb=new StringBuilder
				("select t from Todo t where 1=1");
		
		List<Object> params=new ArrayList<>();
		int pos=0;
		
		//件名
		if(todoQuery.getTitle().length()>0) {
			sb.append(" and t.title like ?"+(++pos));
			params.add("%"+todoQuery.getTitle()+"%");
		}
		
		//重要度
		if(todoQuery.getImportance()!=-1) {
			sb.append(" and t.importance=?"+(++pos));
			params.add(todoQuery.getImportance());
		}
		
		//緊急度
		if(todoQuery.getUrgency()!=-1) {
			sb.append(" and t.urgency=?"+(++pos));
			params.add(todoQuery.getUrgency());
		}
		
		//期限開始～
		if(!todoQuery.getDeadlineFrom().equals("")) {
			sb.append(" and t.deadline>=?"+(++pos));
			params.add(Utils.str2date(todoQuery.getDeadlineFrom()));
		}
		
		//～期限終了
		if(!todoQuery.getDeadlineTo().equals("")) {
			sb.append(" and t.deadline<=?"+(++pos));
			params.add(Utils.str2date(todoQuery.getDeadlineTo()));
		}
		
		//完了
		if(todoQuery.getDone()!=null&&todoQuery.getDone().equals("Y")) {
			sb.append(" and t.done=?"+(++pos));
			params.add(todoQuery.getDone());
		}
		
		sb.append(" order by id");
		
		//Typed<Todo>を生成
		TypedQuery<Todo> typedQuery=entityManager.createQuery(sb.toString(),Todo.class);
		
			//上のTyped<Todo>の?に値を入れる
		for(int i=0;i<params.size();++i) 
			typedQuery=typedQuery.setParameter(i+1, params.get(i));
		
		//該当レコード件数
		int totalRows=typedQuery.getResultList().size();
		
		//先頭レコード位置
		typedQuery.setFirstResult(pageable.getPageNumber()*pageable.getPageSize());
		
		//１ページ当たりの件数
		typedQuery.setMaxResults(pageable.getPageSize());
		
		//現在のページのデータ取得
		List<Todo> todos=typedQuery.getResultList();
		
		Page<Todo> page=new PageImpl<>(todos,pageable,totalRows);
		
		return page;
	}
	
	//Criteria APIによる検索
	@Override
	public List<Todo> findByCriteria(TodoQuery todoQuery){
		return null;
	}
}
