package com.crudstudy.board.repository;

import com.crudstudy.board.domain.File;
import com.crudstudy.board.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File,Long> {

    /**
     * @Modifying
     *  :   jpa의 기본생성 쿼리 => select
     *      delete나 update 쿼리라면 명시 필수
     *  @Transactional : 데이터 변경 작업은 무조건 트랜잭션 안에서 실행
     */
    @Transactional
    @Modifying
    void deleteByPostId(Long postId);

    @Transactional
    @Modifying
    void deleteAllByIdIn(List<Long> ids);

    //해당 글의 파일조회용
    List<File> findByPostId(Long postId);
}
