import { useEffect, useState } from "react"
import { SearchPost } from "../../api/apis/postApi";
import { type PostSearchParams, type PostListResponse } from "../../types/Post";
import { downloadFile } from "../../api/apis/fileApi";
import { useNavigate } from "react-router-dom";
import { logout } from "../../api/apis/authApi";
import { SearchBar } from "./SearchBar";

export const HomePage = () => {
    /**
     * pageable은 0-based
     * 클라는 1-based
     * 서버에서 클라에서 받은 page에 -1을 하여 계산처리
     */
    const [page, setPage] = useState<number>(0);
    const [data, setData] = useState<PostListResponse | null>(null);
    const navigate = useNavigate();
    //home에서 state관리 - 모든 컴포넌트가 같은 값 공유하기 위해서
    const [searchParams, setSearchParams] = useState<PostSearchParams>({
        keyword: "",
        type: "",
        startDate: null,
        endDate: null
    })
    const [openFilePostId, setOpenFilePostId] = useState<number | null>(null);

    const GROUP_SIZE = 5;

    //api 비동기 대비 if문처리
    const pageNumbers = []; //현재 그룹의 페이지 번호 배열
    let currentGroup = 0; //현재 페이지가 속한 그룹번호
    if (data) {
        currentGroup = Math.floor((page) / GROUP_SIZE);
        //현재그룹의 첫번째 번호
        const groupStart = currentGroup * GROUP_SIZE;
        //현재그룹의 마지막번호
        const groupEnd = Math.min(groupStart + GROUP_SIZE - 1, data.totalPages);
        //groupStart ~ groupEnd 범위의 페이지 번호를 배열에 추가
        for (let i = groupStart; i <= groupEnd; i++) {
            pageNumbers.push(i);
        }
    }
    //Array.from 으로 배열만들기 가능 <- 보충공부

    const handleLogout = () => {
        logout()
            .then(() => navigate("/login"))
    }

    useEffect(() => {
        SearchPost({
            ...searchParams,
            startDate: searchParams.startDate ? searchParams.startDate + "T00:00:00" : null,
            endDate: searchParams.endDate ? searchParams.endDate + "T23:59:59" : null,
        }, page).then(res => {
            setData(res.data)
        });
    }, [page]);

    //api호출도 홈에서 - state로 관리되는 data에 접근하기 위해서
    const handleSearch = () => {
        SearchPost({
            ...searchParams,
            startDate: searchParams.startDate ? searchParams.startDate + "T00:00:00" : null,
            endDate: searchParams.endDate ? searchParams.endDate + "T23:59:59" : null,
        }, page).then(res => {
            setData(res.data)
            console.log(res.data)
        });
    };

    return (
        <div>
            <button onClick={handleLogout}>로그아웃</button>
            <h1>게시판</h1>
            <p>
                전체 {data?.totalElements}건 {page + 1}/{data?.totalPages}페이지
            </p>
            <SearchBar
                searchParams={searchParams}
                setSearchParams={setSearchParams}
                onSearch={handleSearch}
            />
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
                    {data?.content
                        .filter(post => !post.isDeleted)
                        .map((post, index) => (
                            /**
                             * navigate로 이동하기
                             * 컴포넌트로 이동하기
                             */
                            <tr key={post.postId} onClick={() => navigate(`/posts/${post.postId}`)}>
                                <td>{page * data.size + index + 1}</td>
                                <td>{post.title}</td>
                                <td>{post.userName}</td>
                                <td>{post.createdAt.split("T")[0]}</td>
                                <td>{post.viewCount}</td>
                                <td>
                                    {post.files.length > 0 ?
                                        <span>
                                            <span onClick={(e) => {
                                                e.stopPropagation()
                                                setOpenFilePostId(openFilePostId === post.postId ? null : post.postId)
                                            }}>
                                                📄
                                            </span>
                                            {openFilePostId === post.postId &&
                                                <div>
                                                    {post.files.map((file, index) =>
                                                        <div key={file.fileId}
                                                            onClick={(e) => {
                                                                e.stopPropagation()
                                                                downloadFile(file.fileId);
                                                            }}>
                                                            📄{index + 1}.{file.fileName}
                                                        </div>
                                                    )}
                                                </div>
                                            }
                                        </span>
                                        : ""}
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
                        <button onClick={() => setPage(0)} disabled={data.first}>처음</button>
                        <button onClick={() => setPage(Math.max(1, (currentGroup - 1) * GROUP_SIZE))}
                            disabled={currentGroup === 0}>이전 10</button> {/* 이전 10페이지 */}
                        {pageNumbers.map(p => (
                            <button key={p} onClick={() => setPage(p)}>
                                {p + 1}
                            </button>
                        ))}
                        <button onClick={() => setPage((currentGroup + 1) * GROUP_SIZE)}
                            disabled={currentGroup === Math.floor((data.totalPages - 1) / GROUP_SIZE)}>다음 10</button> {/* 다음 10페이지 */}
                        <button onClick={() => setPage(data.totalPages - 1)} disabled={data.last}>마지막</button>
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