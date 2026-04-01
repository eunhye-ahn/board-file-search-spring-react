import { useEffect, useState } from "react"
import { getAllPosts } from "../../api/apis/postApi";
import type { PostListResponse } from "../../types/Post";
import { downloadFile } from "../../api/apis/fileApi";
import { useNavigate } from "react-router-dom";
/*
export interface PostListResponse {
    content: PostItem[], //게시글 목록
    totalPage: number,  //페이지버튼 1-totalPages 만들기
    first: boolean, //처음버튼
    last: boolean,  //마지막버튼
    number: number //현재페이지
}

export interface PostItem {
    title: string,
    createdAt: string,
    viewCount: number
}
*/

export const HomePage = () => {
    const [page, setPage] = useState(1);
    const [data, setData] = useState<PostListResponse | null>(null);
    const navigate = useNavigate();

    useEffect(() => {
        getAllPosts(page).then(res => setData(res.data))
        console.log(data);
    }, [page]);

    return (
        <div>
            <h1>게시판</h1>
            <button onClick={() => navigate("/posts")}>글 작성</button>
            <table>
                <thead>
                    <tr>
                        <th>번호</th>
                        <th>제목</th>
                        <th>등록일</th>
                        <th>조회</th>
                        <th>첨부</th>
                    </tr>
                </thead>
                <tbody>
                    {data?.content.map((post, index) => (
                        /**
                         * navigate로 이동하기
                         * 컴포넌트로 이동하기
                         */
                        <tr onClick={() => navigate(`/posts/${post.postId}`)}>
                            <td>{index + 1}</td>
                            <td>{post.title}</td>
                            <td>{post.createdAt}</td>
                            <td>{post.viewCount}</td>
                            <td>
                                {post.files.map(file => (
                                    <span key={file.fileId} onClick={() => downloadFile(file.fileId)}>📎</span>
                                ))}
                            </td>
                        </tr>
                    )) ?? <tr>
                            <td colSpan={4}>포스트가 없습니다</td>
                        </tr>
                    }
                </tbody>
            </table>
            <div>
                <button>처음</button>
                <button>이전 10</button> {/* 이전 10페이지 */}
                <button>1</button>
                <button>2</button>
                <button>3</button>
                <button>다음 10</button> {/* 다음 10페이지 */}
                <button>마지막</button>
            </div>
        </div>
    )
}

/**
 * 1. App.tsx 라우팅
2. HomePage 레이아웃 (UI만 먼저)
3. API 연결해서 데이터 붙이기
4. 나머지 페이지 반복
 */