import { Routes } from '@angular/router';
import { CourseList } from './features/courses/course-list/course-list';


export const routes: Routes = [
  { path: 'courses', component: CourseList },
  { path: '', redirectTo: '/courses', pathMatch: 'full' }
];
