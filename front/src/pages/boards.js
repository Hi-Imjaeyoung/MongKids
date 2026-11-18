// 게시물 상세·작성 화면을 함께 쓰는 게시판 설정. 수업 현장(SCR-006)과 몽키즈 연구소(SCR-024)는 API만 다르다
import {
  createLabPost,
  deleteLabPost,
  getLabPost,
  getLabPostLike,
  likeLabPost,
  unlikeLabPost,
  updateLabPost,
  uploadLabPostImages,
} from '../api/labPosts'
import {
  createPost,
  deletePost,
  getPost,
  getPostLike,
  likePost,
  unlikePost,
  updatePost,
  uploadPostImages,
} from '../api/posts'
import { labCategories, labEditorNotice, labNotice } from '../data/labContent'
import { newsCategories } from '../data/newsContent'
import { pageMeta } from '../data/seoContent'

export const newsBoard = {
  basePath: '/news',
  categories: newsCategories,
  // 상세 화면 탭 제목·설명의 기본값 (NFR-006)
  meta: pageMeta.news,
  // 댓글(FR-010)은 수업 현장 게시물에만 있다
  hasComments: true,
  // 사진만 형식(카드뉴스, SCR-015)은 수업 현장 작성에서만 고를 수 있다
  allowPhotoOnly: true,
  getPost,
  createPost,
  updatePost,
  deletePost,
  uploadImages: uploadPostImages,
  getLike: getPostLike,
  like: likePost,
  unlike: unlikePost,
}

export const labBoard = {
  basePath: '/lab',
  categories: labCategories,
  meta: pageMeta.lab,
  detailNotice: labNotice,
  editorNotice: labEditorNotice,
  getPost: getLabPost,
  createPost: createLabPost,
  updatePost: updateLabPost,
  deletePost: deleteLabPost,
  uploadImages: uploadLabPostImages,
  getLike: getLabPostLike,
  like: likeLabPost,
  unlike: unlikeLabPost,
}
