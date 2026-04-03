import { useEffect, useState } from "react"
import { getAllPosts } from "../../api/apis/postApi";
import type { PostListResponse } from "../../types/Post";
import { downloadFile } from "../../api/apis/fileApi";
import { useNavigate } from "react-router-dom";
import { logout } from "../../api/apis/authApi";
/*
export interface PostListResponse {
    content: PostItem[], //게시글 목록
    totalPage: number,  //페이지버튼 1-totalPages 만들기
    first: boolean, //처음버튼
    last: boolean,  //마지막버튼
    number: number //현재페이지
}
*/

export const HomePage = () => {
    /**
     * pageable은 0-based
     * 클라는 1-based
     * 서버에서 클라에서 받은 page에 -1을 하여 계산처리
     */
    const [page, setPage] = useState(1);
    const GROUP_SIZE = 10;
    const [data, setData] = useState<PostListResponse | null>(null);
    const navigate = useNavigate();

    useEffect(() => {
        getAllPosts(page).then(res => setData(res.data))
    }, [page]);

    //api 비동기 대비 if문처리
    const pageNumbers = [];
    let currentGroup = 0;
    if (data) {
        currentGroup = Math.floor((page - 1) / GROUP_SIZE); //그룹나누기 0~시작
        const groupStart = currentGroup * GROUP_SIZE + 1; //l_based
        const groupEnd = Math.min(groupStart + GROUP_SIZE - 1, data.totalPages);
        for (let i = groupStart; i <= groupEnd; i++) {
            pageNumbers.push(i);

        }
    }
    //Array.from 으로 배열만들기 가능 <- 보충공부

    const handleLogout = () => {
        logout()
            .then(() => navigate("/login"))
    }

    return (
        <div>
            <button onClick={handleLogout}>로그아웃</button>
            <h1>게시판</h1>
            <button onClick={() => navigate("/posts")}>글 작성</button>
            <table>
                <thead>
                    <tr>
                        <th>번호</th>
                        <th>제목</th>
                        <th>작성자</th>
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
                            <td>{post.userName}</td>
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
                {data && (
                    <>
                        <button onClick={() => setPage(1)} disabled={data.first}>처음</button>
                        <button onClick={() => setPage(Math.max(1, (currentGroup - 1) * GROUP_SIZE + 1))}
                            disabled={currentGroup === 0}>이전 10</button> {/* 이전 10페이지 */}
                        {pageNumbers.map(p => (
                            <button key={p} onClick={() => setPage(p)}>
                                {p}
                            </button>
                        ))}
                        <button onClick={() => setPage((currentGroup + 1) * GROUP_SIZE + 1)}
                            disabled={currentGroup === Math.floor((data.totalPages - 1) / GROUP_SIZE)}>다음 10</button> {/* 다음 10페이지 */}
                        <button onClick={() => setPage(data.totalPages)} disabled={data.last}>마지막</button>
                    </>
                )}
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