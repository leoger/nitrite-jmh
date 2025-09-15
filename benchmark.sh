#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'USAGE'
Usage: ./benchmark.sh [-m modules] [-j "JMH args"]

Options:
  -m modules   Comma-separated list of modules to benchmark (v3, v4).
               Defaults to both modules.
  -j "args"    Additional arguments to forward to the JMH runner.
  -h           Show this help message and exit.
USAGE
}

selected_modules=("v3" "v4")
declare -a jmh_args=()

while getopts ":m:j:h" opt; do
  case "${opt}" in
    m)
      if [[ -z "${OPTARG}" ]]; then
        echo "Error: -m requires a comma-separated list of modules." >&2
        usage
        exit 1
      fi

      IFS=',' read -r -a parsed_modules <<< "${OPTARG}"
      selected_modules=()
      for module in "${parsed_modules[@]}"; do
        module="${module//[[:space:]]/}"
        if [[ -z "${module}" ]]; then
          continue
        fi
        case "${module}" in
          v3|nitrite-v3)
            selected_modules+=("v3")
            ;;
          v4|nitrite-v4)
            selected_modules+=("v4")
            ;;
          *)
            echo "Error: Unknown module '${module}'." >&2
            usage
            exit 1
            ;;
        esac
      done

      if (( ${#selected_modules[@]} == 0 )); then
        echo "Error: No valid modules specified." >&2
        usage
        exit 1
      fi
      ;;
    j)
      if [[ -n "${OPTARG}" ]]; then
        read -r -a jmh_args <<< "${OPTARG}"
      else
        jmh_args=()
      fi
      ;;
    h)
      usage
      exit 0
      ;;
    :)
      echo "Error: Option -${OPTARG} requires an argument." >&2
      usage
      exit 1
      ;;
    ?)
      echo "Error: Invalid option -${OPTARG}." >&2
      usage
      exit 1
      ;;
  esac
done
shift $((OPTIND - 1))

if (( $# > 0 )); then
  echo "Error: Unexpected arguments: $*" >&2
  usage
  exit 1
fi

mvn package

mkdir -p reports

for module in "${selected_modules[@]}"; do
  case "${module}" in
    v3)
      module_dir="nitrite-v3"
      ;;
    v4)
      module_dir="nitrite-v4"
      ;;
    *)
      echo "Error: Unsupported module identifier '${module}'." >&2
      exit 1
      ;;
  esac

  jar_path="${module_dir}/target/benchmarks.jar"
  if [[ ! -f "${jar_path}" ]]; then
    echo "Error: Benchmark jar not found for ${module_dir}: ${jar_path}" >&2
    exit 1
  fi

  report_file="reports/${module_dir}.json"
  cmd=("java" "-jar" "${jar_path}" "-rf" "json" "-rff" "${report_file}")
  if (( ${#jmh_args[@]} > 0 )); then
    cmd+=("${jmh_args[@]}")
  fi

  echo "Running benchmarks for ${module_dir}..."
  "${cmd[@]}"
done

