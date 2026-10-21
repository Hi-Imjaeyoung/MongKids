// 게시물 상세·작성 화면을 함께 쓰는 게시판 설정. 수업 현장(SCR-006)과 몽키즈 연구소(SCR-024)는 API만 다르다
import {
  createLabPost,
  deleteLabPost,
  getLabPost,
  getLabPostLike,
  likeLabPost,
  unlikeLabPost,
  updateLabPost,
} from '../api/labPosts'
import { createPost, deletePost, getPost, getPostLike, likePost, unlikePost, updatePost } from '../api/posts'
import { labCategories, labEditorNotice, labNotice } from '../data/labContent'
import { newsCategories } from '../data/newsContent'

export const newsBoard = {
  basePath: '/news',
  categories: newsCategories,
  // 댓글(FR-010)은 수업 현장 게시물에만 있다
  hasComments: true,
  getPost,
  createPost,
  updatePost,
  deletePost,
  getLike: getPostLike,
  like: likePost,
  unlike: unlikePost,
}

export const labBoard = {
  basePath: '/lab',
  categories: labCategories,
  detailNotice: labNotice,
  editorNotice: labEditorNotice,
  getPost: getLabPost,
  createPost: createLabPost,
  updatePost: updateLabPost,
  deletePost: deleteLabPost,
  getLike: getLabPostLike,
  like: likeLabPost,
  unlike: unlikeLabPost,
}
