export interface ApplicationResponseDTO {
  id: number;
  candidateId: number;
  FullName: string;
  isInternal: boolean;
  Email: string;
  Phone: string;
  status: string;
  score: number;
  submissionDate: string;
}
// Application Details Response Interface
export interface ApplicationDetailsResponseDTO {
  applicationId: number;
  resume: Resume;
  matchResult: MatchResult;
}

export interface Resume {
  id: number;
  candidateInfo: CandidateInfo;
  certifications: Certification[];
  education: Education[];
  experience: Experience[];
  languages: Language[];
  projects: Project[];
  skills: Skills;
  createdAt: string;
  updatedAt: string;
}

export interface CandidateInfo {
  id: number;
  email: string;
  linkedin: string;
  location: Location;
  name: string;
  phone: string;
  website: string | null;
}

export interface Location {
  id: number;
  city: string;
  country: string;
  state: string | null;
}

export interface Certification {
  id: number;
  name: string;
  issuer: string;
  date: string;
  // Add any other certification properties that might be needed
}

export interface Education {
  id: number;
  degree: string;
  endDate: string;
  fieldOfStudy: string;
  institution: string;
  location: string;
  startDate: string;
}

export interface Experience {
  id: number;
  achievements: string[];
  company: string;
  endDate: string;
  location: string;
  responsibilities: string[];
  startDate: string;
  title: string;
}

export interface Language {
  id: number;
  language: string;
  proficiency: string;
}

export interface Project {
  id: number;
  name: string;
  description: string;
  technologies: string[];
  url: string | null;
}

export interface Skills {
  id: number;
  soft: string[];
  technical: string[];
}

export interface MatchResult {
  id: number;
  score: number;
  interpretation: string;
  details: MatchResultDetails;
  raw_score: number;
  red_flags: string[];
  bonus_points: string[];
  role_type: string;
  role_confidence: number;
  adapted_criteria: AdaptedCriterion[];
  role_specific_insights: string[];
}

export interface MatchResultDetails {
  id: number;
  education: MatchCategory;
  certifications: MatchCategory;
  skills_match: MatchCategory;
  relevant_experience: MatchCategory;
  cultural_fit: MatchCategory;
  language_proficiency: MatchCategory;
  achievements_projects: MatchCategory;
}

export interface MatchCategory {
  id: number;
  analysis: string;
  raw_score: number;
  weighted_score: number;
  matching_skills: string[];
  missing_skills: string[];
}

export interface AdaptedCriterion {
  id: number;
  name: string;
  weight: number;
}
