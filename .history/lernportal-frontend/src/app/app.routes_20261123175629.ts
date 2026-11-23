import { Routes } from '@angular/router';
import { CourseListComponent } from './features/courses/course-list';


export const routes: Routes = [
  { path: 'courses', component: CourseListComponent },
  { path: '', redirectTo: '/courses', pathMatch: 'full' }
];
